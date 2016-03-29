package org.patientview.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.builder.ConditionBuilder;
import org.patientview.builder.ConditionsBuilder;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.enums.DiagnosisSeverityTypes;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.ConditionService;
import org.patientview.util.Util;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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
    public void add(final Patientview data, final FhirLink fhirLink) throws FhirResourceException {

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
    public void add(FhirCondition fhirCondition, FhirLink fhirLink) throws FhirResourceException {
        ConditionBuilder conditionBuilder
                = new ConditionBuilder(null, fhirCondition, Util.createResourceReference(fhirLink.getResourceId()));

        fhirResource.createEntity(conditionBuilder.build(), ResourceType.Condition.name(), "condition");
    }

    private void deleteBySubjectId(UUID subjectId) throws FhirResourceException {
        // native delete, ignore with severity MAIN as used by ibd patient management
        fhirResource.executeSQL(
                "DELETE FROM condition WHERE CONTENT -> 'subject' ->> 'display' = '" + subjectId.toString() + "' " +
                "AND CONTENT -> 'severity' ->> 'text' != '" + DiagnosisSeverityTypes.MAIN.toString() + "'"
        );
    }

    @Override
    public void deleteBySubjectIdAndType(UUID subjectId, DiagnosisTypes diagnosisTypes) throws FhirResourceException {
        // native delete
        fhirResource.executeSQL(
                "DELETE FROM condition WHERE CONTENT -> 'subject' ->> 'display' = '" + subjectId.toString() + "' " +
                "AND CONTENT -> 'category' ->> 'text' = '" + diagnosisTypes.toString() + "'"
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

    @Override
    public FhirDatabaseEntity update(FhirCondition fhirCondition, FhirLink fhirLink, UUID existingConditionUuid)
            throws FhirResourceException {
        Condition existingCondition = (Condition) fhirResource.get(existingConditionUuid, ResourceType.Condition);

        if (existingCondition == null) {
            throw new FhirResourceException("error getting existing Condition");
        }

        return fhirResource.updateEntity(new ConditionBuilder(
            existingCondition, fhirCondition, Util.createResourceReference(fhirLink.getResourceId())).build(),
                ResourceType.Condition.name(), "condition", existingConditionUuid);
    }
}
