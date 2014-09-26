package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.importer.builder.ObservationsBuilder;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.importer.resource.FhirResource;
import org.patientview.importer.service.ObservationService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Service
public class ObservationServiceImpl extends AbstractServiceImpl<ObservationService> implements ObservationService {

    @Inject
    private FhirResource fhirResource;

    /**
     * Creates all of the FHIR observation records from the Patientview object. Links then to the PatientReference
     *
     * @param data
     * @param patientReference
     */
    @Override
    public void add(final Patientview data, final ResourceReference patientReference) {

        LOG.info("Starting Observation Process");

        ObservationsBuilder observationsBuilder = new ObservationsBuilder(data, patientReference);
        int count = 0;
        for (Observation observation : observationsBuilder.build()) {
            LOG.trace("Creating... observation " + count);
            try {
                fhirResource.create(observation);
            } catch (FhirResourceException e) {
                LOG.error("Unable to build observation");
            }
            LOG.trace("Finished creating observation " + count++);
        }
        LOG.info("Processed {} of {} observations", observationsBuilder.getSuccess(), observationsBuilder.getCount());
    }

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException {
        for (UUID uuid : fhirResource.getLogicalIdsBySubjectId("observation", subjectId)) {
            fhirResource.delete(uuid, ResourceType.Observation);
        }
    }
}


