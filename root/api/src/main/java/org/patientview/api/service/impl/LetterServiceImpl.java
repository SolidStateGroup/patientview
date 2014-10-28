package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.DocumentReference;
import org.patientview.api.controller.BaseController;
import org.patientview.persistence.model.FhirLetter;
import org.patientview.api.service.LetterService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Service
public class LetterServiceImpl extends BaseController<LetterServiceImpl> implements LetterService {

    @Inject
    private FhirResource fhirResource;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<FhirLetter> getByUserId(final Long userId) throws ResourceNotFoundException, FhirResourceException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        List<FhirLetter> fhirLetters = new ArrayList<>();

        for (FhirLink fhirLink : user.getFhirLinks()) {
            if (fhirLink.getActive()) {
                StringBuilder query = new StringBuilder();
                query.append("SELECT  content::varchar ");
                query.append("FROM    documentreference ");
                query.append("WHERE   content->> 'subject' = '{\"display\": \"");
                query.append(fhirLink.getResourceId().toString());
                query.append("\", \"reference\": \"uuid\"}'");

                // get list of DocumentReference
                List<DocumentReference> documentReferences
                    = fhirResource.findResourceByQuery(query.toString(), DocumentReference.class);

                // for each, create new transport object
                for (DocumentReference documentReference : documentReferences) {
                    fhirLetters.add(new FhirLetter(documentReference, fhirLink.getGroup()));
                }
            }
        }

        return fhirLetters;
    }
}
