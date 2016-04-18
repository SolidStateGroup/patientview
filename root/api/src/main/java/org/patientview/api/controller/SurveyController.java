package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.service.SurveyService;
import org.patientview.persistence.model.Survey;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * RESTful interface for Surveys, used by IBD
 *
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
@ExcludeFromApiDoc
@RestController
public class SurveyController extends BaseController<SurveyController> {

    @Inject
    private SurveyService surveyService;

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
}
