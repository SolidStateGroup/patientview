package org.patientview.api.service.impl;

import org.patientview.api.service.SymptomScoreService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.SymptomScore;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.SymptomScoreRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
@Service
public class SymptomScoreServiceImpl extends AbstractServiceImpl<SymptomScoreServiceImpl>
        implements SymptomScoreService {

    @Inject
    private SymptomScoreRepository symptomScoreRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public List<SymptomScore> getByUserId(final Long userId)
            throws ResourceNotFoundException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        return symptomScoreRepository.findByUser(user);
    }
}
