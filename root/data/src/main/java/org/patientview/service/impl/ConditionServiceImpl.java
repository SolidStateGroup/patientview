package org.patientview.service.impl;

import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.builder.ConditionsBuilder;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.ConditionService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Service
public class ConditionServiceImpl extends AbstractServiceImpl<ConditionService> implements ConditionService {

    @Inject
    private FhirResource fhirResource;

    private String nhsno;

    /**
     * Creates all of the FHIR condition records from the Patientview object. Links them to the PatientReference
     */
    @Override
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException, SQLException {

        this.nhsno = data.getPatient().getPersonaldetails().getNhsno();
        int count = 0;
        LOG.trace(nhsno + ": Starting Condition Process");
        ResourceReference patientReference = Util.createResourceReference(fhirLink.getResourceId());
        ConditionsBuilder conditionsBuilder = new ConditionsBuilder(data, patientReference);

        // delete old Condition attached to this patient
        deleteBySubjectId(fhirLink.getResourceId());

        for (Condition condition : conditionsBuilder.build()) {
            LOG.trace(nhsno + ": Creating... condition " + count);
            try {
                fhirResource.createEntity(condition, ResourceType.Condition.name(), "condition");
            } catch (FhirResourceException e) {
                LOG.error(nhsno + ": Unable to build condition");
            }
            LOG.trace(nhsno + ": Finished creating condition " + count++);
        }
        LOG.info(nhsno + ": Processed {} of {} conditions",
                conditionsBuilder.getSuccess(), conditionsBuilder.getCount());
    }

    @Override
    public void add(FhirCondition fhirCondition, FhirLink fhirLink)
            throws ResourceNotFoundException, FhirResourceException {

        Condition condition = new Condition();
        condition.setStatusSimple(Condition.ConditionStatus.confirmed);
        condition.setSubject(Util.createResourceReference(fhirLink.getResourceId()));

        if (StringUtils.isNotEmpty(fhirCondition.getNotes())) {
            condition.setNotesSimple(fhirCondition.getNotes());
        }

        if (StringUtils.isNotEmpty(fhirCondition.getCode())) {
            CodeableConcept code = new CodeableConcept();
            code.setTextSimple(fhirCondition.getCode());
            condition.setCode(code);
        }

        if (StringUtils.isNotEmpty(fhirCondition.getCategory())) {
            CodeableConcept category = new CodeableConcept();
            category.setTextSimple(fhirCondition.getCategory());
            condition.setCategory(category);
        }

        if (fhirCondition.getDate() != null) {
            DateAndTime dateAndTime = new DateAndTime(fhirCondition.getDate());
            condition.setDateAssertedSimple(dateAndTime);
        }

        fhirResource.createEntity(condition, ResourceType.Condition.name(), "condition");
    }


    private void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        // native delete
        fhirResource.executeSQL(
                "DELETE FROM condition WHERE CONTENT -> 'subject' ->> 'display' = '" + subjectId.toString() + "'"
        );
    }

    @Override
    public List<Condition> get(final UUID patientUuid) throws FhirResourceException {
        List<Condition> conditions = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    condition ");
        query.append("WHERE   content -> 'subject' ->> 'display' = '");
        query.append(patientUuid);
        query.append("' ");

        conditions.addAll(fhirResource.findResourceByQuery(query.toString(), Condition.class));

        return conditions;
    }

}
