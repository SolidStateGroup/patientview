package org.patientview.importer.manager.impl;

import generated.Patientview;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.importer.service.ObservationService;
import org.patientview.importer.service.PatientService;
import org.patientview.importer.service.impl.AbstractServiceImpl;
import org.patientview.persistence.exception.FhirResourceException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Service
public class ImportManagerImpl extends AbstractServiceImpl<ImportManager> implements ImportManager {

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
            observationService.add(patientview, patientReference);
        } catch (FhirResourceException | ResourceNotFoundException e) {
            LOG.error("Unable to build patient {}", patientview.getPatient().getPersonaldetails().getNhsno());
            throw new ImportResourceException("Could not process patient data");
        }
    }


    private ResourceReference createResourceReference(UUID uuid) {
        ResourceReference resourceReference = new ResourceReference();

        resourceReference.setDisplaySimple(uuid.toString());
        resourceReference.setReferenceSimple("uuid");

        return resourceReference;
    }


}
