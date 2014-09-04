package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.service.PatientService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.util.DataUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
@Service
public class PatientServiceImpl extends AbstractServiceImpl<PatientServiceImpl> implements PatientService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<org.patientview.api.model.Patient> get(final Long userId)
            throws FhirResourceException, ResourceNotFoundException {

        User user = userRepository.findOne(userId);

        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<org.patientview.api.model.Patient> patients = new ArrayList<>();
        List<Group> groups = new ArrayList<>();
        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.addAll(user.getFhirLinks());

        // sort fhirLinks by id
        Collections.sort(fhirLinks, new Comparator<FhirLink>() {
            public int compare(FhirLink f1, FhirLink f2) {
                return f2.getCreated().compareTo(f1.getCreated());
            }
        });

        // get data from FHIR from each unit, ignoring multiple FHIR records per unit (versions)
        for (FhirLink fhirLink : fhirLinks) {
            if (!groups.contains(fhirLink.getGroup())) {
                patients.add(new org.patientview.api.model.Patient(get(fhirLink.getResourceId()), fhirLink.getGroup()));
                groups.add(fhirLink.getGroup());
            }
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
