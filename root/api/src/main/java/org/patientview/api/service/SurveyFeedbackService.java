package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.SurveyFeedback;

import java.util.List;

/**
 * SurveyFeedback service
 *
 * Created by jamesr@solidstategroup.com
 * Created on 18/05/2016
 */
public interface SurveyFeedbackService {

    /**
     * Add a new SurveyFeedback associated with a User and a Survey
     *
     * @param userId Id of User
     * @param surveyFeedback SurveyFeedback containing feedback and reference to Survey
     * @throws ResourceNotFoundException
     * @throws VerificationException
     */
    @UserOnly
    void add(Long userId, SurveyFeedback surveyFeedback) throws ResourceNotFoundException, VerificationException;

    /**
     * Get List of SurveyFeedback associated with a User and a Survey
     *
     * @param userId Id of User
     * @param surveyId Id of Survey
     * @return List of SurveyFeedback associated with a User and a Survey
     * @throws ResourceNotFoundException
     */
    @UserOnly
    List<org.patientview.api.model.SurveyFeedback> getByUserIdAndSurveyId(Long userId, Long surveyId)
            throws ResourceNotFoundException;


    /**
     * Hard delete all SurveyFeedback entries associated with a User.
     *
     * @param userId a User ID to delete SurveyFeedback entries for
     */
    void deleteForUser(Long userId);
}
