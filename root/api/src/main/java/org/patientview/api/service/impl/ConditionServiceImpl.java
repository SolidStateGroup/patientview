package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Observation;
import org.patientview.api.controller.BaseController;
import org.patientview.api.service.ConditionService;
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
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Service
public class ConditionServiceImpl extends BaseController<ConditionServiceImpl> implements ConditionService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<Condition> get(final Long userId, final String code) throws ResourceNotFoundException, FhirResourceException {

        List<Condition> conditions = new ArrayList<>();

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        for (FhirLink fhirLink : user.getFhirLinks()) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT  content::varchar ");
            query.append("FROM    condition ");
            query.append("WHERE   content->> 'subject' = '{\"display\": \"");
            query.append(fhirLink.getVersionId().toString());
            query.append("\", \"reference\": \"uuid\"}'");
            conditions.addAll(fhirResource.findResourceByQuery(query.toString(), Condition.class));
        }

        return conditions;
    }

    @Override
    public List<Condition> get(final UUID patientUuid) throws FhirResourceException{
        List<Condition> conditions = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT  content::varchar ");
        query.append("FROM    condition ");
        query.append("WHERE   content->> 'subject' = '{\"display\": \"");
        query.append(patientUuid);
        query.append("\", \"reference\": \"uuid\"}'");
        conditions.addAll(fhirResource.findResourceByQuery(query.toString(), Condition.class));

        return conditions;
    }
}
