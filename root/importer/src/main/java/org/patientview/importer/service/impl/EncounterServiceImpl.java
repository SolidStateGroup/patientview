package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.EncountersBuilder;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.EncounterService;
import org.patientview.persistence.exception.FhirResourceException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class EncounterServiceImpl extends AbstractServiceImpl<EncounterService> implements EncounterService{

    @Inject
    private FhirResource fhirResource;

    /**
     * Creates FHIR encounter (treatment and transplant details) records from the Patientview object.
     *
     * @param data
     */
    @Override
    public void add(final Patientview data, final ResourceReference patientReference,
                    final ResourceReference groupReference) {
        EncountersBuilder encountersBuilder = new EncountersBuilder(data, patientReference, groupReference);

        LOG.info("Starting Encounter Process");

        int count = 0;
        for (Encounter encounter : encountersBuilder.build()) {
            LOG.trace("Creating... encounter " + count);
            try {
                fhirResource.create(encounter);
            } catch (FhirResourceException e) {
                LOG.error("Unable to build encounter");
            }
            LOG.trace("Finished creating encounter " + count++);
        }
        LOG.info("Processed {} of {} encounters", encountersBuilder.getSuccess(), encountersBuilder.getCount());
    }

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("encounter", subjectId)) {
            fhirResource.delete(uuid, ResourceType.Encounter);
        }
    }
}


