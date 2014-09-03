package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.service.PatientService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.util.DataUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public class PatientServiceImpl extends AbstractServiceImpl<PatientServiceImpl> implements PatientService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<Patient> get(final Long userId) throws FhirResourceException, ResourceNotFoundException {

        User user = userRepository.findOne(userId);

        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<Patient> patients = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            patients.add(get(fhirLink.getResourceId()));
        }

        return patients;
    }

    @Override
    public Patient get(final UUID uuid) throws FhirResourceException {
        try {
            return (Patient) DataUtils.getResource(fhirResource.getResource(uuid, ResourceType.Patient));
        } catch (Exception e) {
            throw new FhirResourceException(e);
        }
    }
}
