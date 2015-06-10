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
    public void add(Long userId, SymptomScore symptomScore) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }


    }

    @Override
    public List<SymptomScore> getByUserId(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        // testing
        /*DateTime now = new DateTime(new Date());
        List<SymptomScore> symptomScores = new ArrayList<>();
        symptomScores.add(new SymptomScore(user, 0.1, ScoreSeverity.LOW, now.minusMonths(23).toDate()));
        symptomScores.add(new SymptomScore(user, 1.1, ScoreSeverity.LOW, now.minusMonths(3).toDate()));
        symptomScores.add(new SymptomScore(user, 2.1, ScoreSeverity.MEDIUM, now.minusMonths(2).toDate()));
        symptomScores.add(new SymptomScore(user, 3.1, ScoreSeverity.HIGH, now.minusMonths(1).toDate()));
        return symptomScores;*/

        return symptomScoreRepository.findByUser(user);
    }

    @Override
    public SymptomScore getSymptomScore(Long userId, Long symptomScoreId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        // testing
        //return new SymptomScore(user, 1.1, ScoreSeverity.LOW, new Date());

        return symptomScoreRepository.findOne(symptomScoreId);
    }
}
