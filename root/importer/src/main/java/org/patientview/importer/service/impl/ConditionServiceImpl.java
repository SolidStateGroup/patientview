package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.ConditionsBuilder;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.ConditionService;
import org.patientview.persistence.exception.FhirResourceException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Service
public class ConditionServiceImpl extends AbstractServiceImpl<ConditionService> implements ConditionService{

    @Inject
    private FhirResource fhirResource;

    /**
     * Creates all of the FHIR condition records from the Patientview object. Links them to the PatientReference
     *
     * @param data
     * @param patientReference
     */
    @Override
    public void add(final Patientview data, final ResourceReference patientReference) {

        LOG.info("Starting Condition Process");

        ConditionsBuilder conditionsBuilder = new ConditionsBuilder(data, patientReference);
        int count = 0;
        for (Condition condition : conditionsBuilder.build()) {
            LOG.trace("Creating... condition " + count);
            try {
                fhirResource.create(condition);
            } catch (FhirResourceException e) {
                LOG.error("Unable to build condition");
            }
            LOG.trace("Finished creating condition " + count++);
        }
        LOG.info("Processed {} of {} conditions", conditionsBuilder.getSuccess(), conditionsBuilder.getCount());
    }

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("condition", subjectId)) {
            fhirResource.delete(uuid, ResourceType.Condition);
        }
    }
}


