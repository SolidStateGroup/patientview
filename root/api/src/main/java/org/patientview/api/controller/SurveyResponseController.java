package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ApiSurveyResponseService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.SurveyResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.util.List;

/**
 * RESTful interface for SurveyResponses
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
@ExcludeFromApiDoc
@RestController
public class SurveyResponseController extends BaseController<SurveyResponseController> {

    @Inject
    private ApiSurveyResponseService apiSurveyResponseService;

    @RequestMapping(value = "/user/{userId}/surveyresponses", method = RequestMethod.POST)
    @ResponseBody
    public void add(@PathVariable("userId") Long userId, @RequestBody SurveyResponse surveyResponse)
            throws ResourceForbiddenException, ResourceNotFoundException,
            JAXBException, DatatypeConfigurationException {
        apiSurveyResponseService.add(userId, surveyResponse);
    }

    @RequestMapping(value = "/user/{userId}/surveyresponses/{surveyResponseId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<SurveyResponse> get(@PathVariable("userId") Long userId,
                                              @PathVariable("surveyResponseId") Long surveyResponseId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(
                apiSurveyResponseService.getSurveyResponse(userId, surveyResponseId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/surveyresponses/type/{surveyType}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<SurveyResponse>> getByUserAndSurveyType(
            @PathVariable("userId") Long userId, @PathVariable("surveyType") String surveyType)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(
                apiSurveyResponseService.getByUserIdAndSurveyType(userId, surveyType), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/surveyresponses/latest", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<SurveyResponse>> getLatestByUserAndSurveyType(
            @PathVariable("userId") Long userId, @RequestParam List<String> types)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(
                apiSurveyResponseService.getLatestByUserIdAndSurveyType(userId, types), HttpStatus.OK);
    }
}
