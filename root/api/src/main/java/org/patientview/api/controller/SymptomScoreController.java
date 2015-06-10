package org.patientview.api.controller;

import org.patientview.api.service.SymptomScoreService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.SymptomScore;
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
@RestController
public class SymptomScoreController extends BaseController<SymptomScoreController> {

    @Inject
    private SymptomScoreService symptomScoreService;

    @RequestMapping(value = "/user/{userId}/symptomscores", method = RequestMethod.POST)
    @ResponseBody
    public void add(@PathVariable("userId") Long userId, @RequestBody SymptomScore symptomScore)
            throws ResourceNotFoundException {
        symptomScoreService.add(userId, symptomScore);
    }

    @RequestMapping(value = "/user/{userId}/symptomscores", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<SymptomScore>> getByUser(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(symptomScoreService.getByUserId(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/symptomscores/{surveyType}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<SymptomScore>> getByUserAndSurveyType(
            @PathVariable("userId") Long userId, @PathVariable("surveyType") SurveyTypes surveyType)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(symptomScoreService.getByUserIdAndSurveyType(userId, surveyType), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/symptomscore/{symptomScoreId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<SymptomScore> get(@PathVariable("userId") Long userId,
                                            @PathVariable("symptomScoreId") Long symptomScoreId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(symptomScoreService.getSymptomScore(userId, symptomScoreId), HttpStatus.OK);
    }
}
