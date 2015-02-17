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
 * RESTful interface for retrieving the patient records associated with a User, retrieved from FHIR.
 *
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
@RestController
public class PatientController  extends BaseController<PatientController> {

    @Inject
    private PatientService patientService;

    /**
     * Get a list of User patient records, as stored in FHIR and associated with Groups that have imported patient data.
     * Produces a larger object containing all the properties required to populate My Details and My Conditions pages.
     * @param userId ID of User to retrieve patient record for
     * @param groupIds IDs of Groups to retrieve patient records from
     * @return List of Patient objects containing patient encounters, conditions etc
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/patient/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<org.patientview.api.model.Patient>> getPatientDetails(
            @PathVariable("userId") Long userId, @RequestParam(value = "groupId", required = false) List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(patientService.get(userId, groupIds), HttpStatus.OK);
    }
}
