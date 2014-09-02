package org.patientview.api.service;

import org.hl7.fhir.instance.model.Observation;
import org.patientview.persistence.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface ObservationService {

    List<Observation> get(User user);

    List<Observation> get(UUID patientUuid);

}
