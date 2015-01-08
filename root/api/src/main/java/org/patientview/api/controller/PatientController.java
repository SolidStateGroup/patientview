package org.patientview.api.controller;

import org.patientview.api.service.PatientService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
@RestController
public class PatientController  extends BaseController<PatientController> {

    @Inject
    private PatientService patientService;

    @RequestMapping(value = "/patient/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<org.patientview.api.model.Patient>> getPatientDetails(
            @PathVariable("userId") Long userId, @RequestParam(value = "groupId", required = false) List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(patientService.get(userId, groupIds), HttpStatus.OK);
    }

}
