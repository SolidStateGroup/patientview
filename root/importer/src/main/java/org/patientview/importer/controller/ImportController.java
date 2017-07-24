package org.patientview.importer.controller;

import generated.Patientview;
import generated.Survey;
import generated.SurveyResponse;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.importer.service.QueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.org.rixg.PatientRecord;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Entry point for the importer
 *
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 */
@RestController
public class ImportController {

    private final static Logger LOG = LoggerFactory.getLogger(ImportController.class);

    @Inject
    QueueService queueService;

    @PostConstruct
    public void init() {
        LOG.info("Import Controller Started");
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getTest() throws ImportResourceException {
        return new ResponseEntity<>("Importer OK", HttpStatus.OK);
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE)
    public void importPatient(@RequestBody Patientview patientview) throws ImportResourceException {
        queueService.importRecord(patientview);
    }

    @RequestMapping(value = "/import/survey", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE)
    public void importSurvey(@RequestBody Survey survey) throws ImportResourceException {
        queueService.importRecord(survey);
    }

    @RequestMapping(value = "/import/surveyresponse", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_XML_VALUE)
    public void importSurveyResponse(@RequestBody SurveyResponse surveyResponse) throws ImportResourceException {
        queueService.importRecord(surveyResponse);
    }

    @RequestMapping(value = "/import/ukrdc", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_XML_VALUE)
    public void importUkrdcPatientRecord(@RequestBody PatientRecord patientRecord) throws ImportResourceException {
        queueService.importRecord(patientRecord);
    }

    @ExceptionHandler(ImportResourceException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleImportResourceException(ImportResourceException e) {
        return e.getMessage();
    }
}
