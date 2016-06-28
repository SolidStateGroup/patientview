package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.SurveyFeedbackService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.SurveyFeedback;
import org.patientview.service.SurveyService;
import org.patientview.persistence.model.Survey;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * RESTful interface for Survey and SurveyFeedback
 *
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
@ExcludeFromApiDoc
@RestController
public class SurveyController extends BaseController<SurveyController> {

    @Inject
    private SurveyService surveyService;

    @Inject
    private SurveyFeedbackService surveyFeedbackService;

    /**
     * Add a new SurveyFeedback associated with a User and a Survey
     *
     * @param userId Id of User
     * @param surveyFeedback SurveyFeedback containing feedback and reference to Survey
     * @throws ResourceNotFoundException
     * @throws VerificationException
     */
    @RequestMapping(value = "/user/{userId}/surveys/feedback", method = RequestMethod.POST)
    @ResponseBody
    public void addFeedback(@PathVariable("userId") Long userId, @RequestBody SurveyFeedback surveyFeedback)
            throws ResourceNotFoundException, VerificationException {
        surveyFeedbackService.add(userId, surveyFeedback);
    }

    /**
     * Get the first stored instance of a Survey given a type.
     *
     * @param type String type of Survey
     * @return First instance of a Survey given type
     */
    @RequestMapping(value = "/surveys/type/{type}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Survey> getByType(@PathVariable("type") String type) {
        return new ResponseEntity<>(surveyService.getByType(type), HttpStatus.OK);
    }

    /**
     * Get List of SurveyFeedback associated with a User and a Survey
     *
     * @param userId Id of User
     * @param surveyId Id of Survey
     * @return List of SurveyFeedback associated with a User and a Survey
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/surveys/{surveyId}/feedback", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<org.patientview.api.model.SurveyFeedback>> getFeedback(
            @PathVariable("userId") Long userId,
            @PathVariable("surveyId") Long surveyId) throws ResourceNotFoundException {
        return new ResponseEntity<>(surveyFeedbackService.getByUserIdAndSurveyId(userId, surveyId), HttpStatus.OK);
    }
}
