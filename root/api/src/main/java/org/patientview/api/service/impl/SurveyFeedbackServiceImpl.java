package org.patientview.api.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.patientview.api.service.SurveyFeedbackService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyFeedback;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.SurveyFeedbackRepository;
import org.patientview.persistence.repository.SurveyRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/05/2016
 */
@Service
public class SurveyFeedbackServiceImpl extends AbstractServiceImpl<SurveyFeedbackServiceImpl>
        implements SurveyFeedbackService {

    @Inject
    private SurveyRepository surveyRepository;

    @Inject
    private SurveyFeedbackRepository surveyFeedbackRepository;

    @Inject
    private UserRepository userRepository;

    @Override
    public void add(Long userId, SurveyFeedback surveyFeedback)
            throws ResourceNotFoundException, VerificationException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        if (surveyFeedback == null) {
            throw new VerificationException("No feedback");
        }

        if (StringUtils.isEmpty(surveyFeedback.getFeedback())) {
            throw new VerificationException("No feedback text provided");
        }

        if (surveyFeedback.getSurvey() == null) {
            throw new ResourceNotFoundException("No survey");
        }

        if (surveyFeedback.getSurvey().getId() == null) {
            throw new ResourceNotFoundException("No survey ID");
        }

        Survey survey = surveyRepository.findOne(surveyFeedback.getSurvey().getId());
        if (survey == null) {
            throw new ResourceNotFoundException("Could not find survey");
        }

        SurveyFeedback toSave = new SurveyFeedback();
        toSave.setUser(user);
        toSave.setSurvey(survey);
        toSave.setFeedback(surveyFeedback.getFeedback());
        toSave.setCreated(new Date());
        toSave.setCreator(user);

        surveyFeedbackRepository.save(toSave);
    }

    private List<org.patientview.api.model.SurveyFeedback> convertSurveyFeedback(List<SurveyFeedback> surveyFeedbacks) {
        List<org.patientview.api.model.SurveyFeedback> toReturn = new ArrayList<>();
        for (SurveyFeedback surveyFeedback : surveyFeedbacks) {
            toReturn.add(new org.patientview.api.model.SurveyFeedback(surveyFeedback));
        }
        return toReturn;
    }

    @Override
    public List<org.patientview.api.model.SurveyFeedback> getByUserIdAndSurveyId(Long userId, Long surveyId)
            throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user");
        }

        Survey survey = surveyRepository.findOne(surveyId);
        if (survey == null) {
            throw new ResourceNotFoundException("Could not find survey");
        }

        return convertSurveyFeedback(surveyFeedbackRepository.findBySurveyAndUser(survey, user));
    }

    @Override
    public void deleteForUser(Long userId) {
        surveyFeedbackRepository.deleteSurveyFeedbackByUser(userId);
    }
}
