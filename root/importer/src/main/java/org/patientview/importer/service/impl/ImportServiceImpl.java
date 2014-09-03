package org.patientview.importer.service.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.service.ImportService;
import org.patientview.importer.service.ObservationService;
import org.patientview.importer.service.PatientService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Service
public class ImportServiceImpl extends AbstractServiceImpl<ImportService> implements ImportService {

    @Inject
    private PatientService patientService;

    @Inject
    private ObservationService observationService;


    @Override
    public void process(Patientview patientview) throws ImportResourceException {
        ResourceReference patientReference;
        try {
            UUID uuid = patientService.add(patientview);
            patientReference = createResourceReference(uuid);
        } catch (FhirResourceException | ResourceNotFoundException e) {
            LOG.error("Unable to build patient {}", patientview.getPatient().getPersonaldetails().getNhsno());
            throw new ImportResourceException("Could not build patient");
        }

        observationService.add(patientview, patientReference);

    }



    private ResourceReference createResourceReference(UUID uuid) {
        ResourceReference resourceReference = new ResourceReference();

        resourceReference.setDisplaySimple(uuid.toString());
        resourceReference.setReferenceSimple("uuid");

        return resourceReference;
    }


}
