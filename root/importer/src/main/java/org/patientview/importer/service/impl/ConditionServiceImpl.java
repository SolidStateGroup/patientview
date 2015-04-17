package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.ConditionsBuilder;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.importer.service.ConditionService;
import org.patientview.importer.Utility.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
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
        LOG.info(nhsno + ": Starting Condition Process");
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

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        for (UUID logicalUuid : fhirResource.getLogicalIdsBySubjectId("condition", subjectId)) {
            fhirResource.deleteEntity(logicalUuid, "condition");
        }
    }
}
