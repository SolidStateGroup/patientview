package org.patientview.api.controller;

import org.patientview.api.service.SymptomScoreService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.SymptomScore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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

    @RequestMapping(value = "/user/{userId}/symptomscores", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<SymptomScore>> getByUser(@PathVariable("userId") Long userId)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(symptomScoreService.getByUserId(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/symptomscores/{symptomScoreId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<SymptomScore> get(@PathVariable("userId") Long userId,
                                            @PathVariable("symptomScoreId") Long symptomScoreId)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(symptomScoreService.getSymptomScore(userId, symptomScoreId), HttpStatus.OK);
    }
}
