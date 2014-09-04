package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.Observation;
import org.patientview.api.controller.BaseController;
import org.patientview.api.service.ObservationService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
@Service
public class ObservationServiceImpl extends BaseController<ObservationServiceImpl> implements ObservationService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<Observation> get(final Long userId, final String code) throws ResourceNotFoundException, FhirResourceException {

        List<Observation> observations = new ArrayList<>();

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        for (FhirLink fhirLink : user.getFhirLinks()) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT  content::varchar ");
            //query.append("SELECT  * ");
            query.append("FROM    observation ");
            query.append("WHERE   content->> 'subject' = '{\"display\": \"");
            query.append(fhirLink.getVersionId().toString());
            query.append("\", \"reference\": \"uuid\"}'");
            observations.addAll(fhirResource.findResourceByQuery(query.toString(), Observation.class));
        }

        return observations;
    }

    @Override
    public List<Observation> get(final UUID patientUuid) {
        return null;
    }
}
