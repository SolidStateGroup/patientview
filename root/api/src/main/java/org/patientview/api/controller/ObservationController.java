package org.patientview.api.controller;

import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.ObservationHeading;
import org.patientview.api.service.ObservationService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.exception.FhirResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
@RestController
public class ObservationController extends BaseController<ObservationController> {

    @Inject
    private ObservationService observationService;

    @RequestMapping(value = "/observation/user/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirObservation>> getAllObservations(@PathVariable("userId") Long userId)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(observationService.get(userId, null, null, null), HttpStatus.OK);
    }

    @RequestMapping(value = "/observation/user/{userId}/{code}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirObservation>> getObservationsByCode(@PathVariable("userId") Long userId,
            @PathVariable("code") String code) throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(observationService.get(userId, code, null, null), HttpStatus.OK);
    }

    @RequestMapping(value = "/observation/user/{userId}/summary", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<HashMap<Long, List<ObservationHeading>>>> getObservationSummary(
            @PathVariable("userId") Long userId) throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(observationService.getObservationSummary(userId), HttpStatus.OK);
    }
}
