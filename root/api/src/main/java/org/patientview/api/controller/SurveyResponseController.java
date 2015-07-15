package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.SurveyResponseService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.enums.SurveyTypes;
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
 * RESTful interface for Symptom Scores, used by IBD
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
@ExcludeFromApiDoc
@RestController
public class SurveyResponseController extends BaseController<SurveyResponseController> {

    @Inject
    private SurveyResponseService surveyResponseService;

    @RequestMapping(value = "/user/{userId}/surveyresponses", method = RequestMethod.POST)
    @ResponseBody
    public void add(@PathVariable("userId") Long userId, @RequestBody SurveyResponse surveyResponse)
            throws ResourceNotFoundException {
        surveyResponseService.add(userId, surveyResponse);
    }

    @RequestMapping(value = "/user/{userId}/surveyresponses/type/{surveyType}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<SurveyResponse>> getByUserAndSurveyType(
            @PathVariable("userId") Long userId, @PathVariable("surveyType") SurveyTypes surveyType)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(surveyResponseService.getByUserIdAndSurveyType(userId, surveyType), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/surveyresponses/{surveyResponseId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<SurveyResponse> get(@PathVariable("userId") Long userId,
            @PathVariable("surveyResponseId") Long surveyResponseId) throws ResourceNotFoundException {
        return new ResponseEntity<>(surveyResponseService.getSurveyResponse(userId, surveyResponseId), HttpStatus.OK);
    }
}
