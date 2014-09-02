package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.patientview.persistence.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface PatientService {

    List<Patient> get(User user);

    Patient get(UUID uuid);


}
