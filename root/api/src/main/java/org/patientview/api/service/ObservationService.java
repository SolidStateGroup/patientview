package org.patientview.api.service;

import org.hl7.fhir.instance.model.Observation;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.ObservationHeading;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 02/09/2014
 */
public interface ObservationService {

    List<FhirObservation> get(Long userId, String code, String orderBy, Long limit)
            throws ResourceNotFoundException, FhirResourceException;

    List<Observation> get(UUID patientUuid);

    List<HashMap<Long, List<ObservationHeading>>> getObservationSummary(Long userId)
            throws ResourceNotFoundException, FhirResourceException;
}
