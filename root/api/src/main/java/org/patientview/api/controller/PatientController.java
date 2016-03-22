package org.patientview.api.controller;

import com.wordnik.swagger.annotations.ApiOperation;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ApiPatientService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirPatient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
    private ApiPatientService apiPatientService;

    /**
     * Get a list of User patient records, as stored in FHIR and associated with Groups that have imported patient data.
     * Produces a list of basic patient information. Used by CKD.
     * @param userId ID of User to retrieve patient record for
     * @return List of Patient objects containing patient information
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @ApiOperation(value = "Get Basic Patient Information", notes = "Given a User ID, get basic patient "
            + "information for a user from clinical data stored in FHIR")
    @RequestMapping(value = "/patient/{userId}/basic", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<org.patientview.api.model.Patient>> getBasicPatientDetails(
            @PathVariable("userId") Long userId) throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(apiPatientService.getBasic(userId), HttpStatus.OK);
    }

    /**
     * Get a list of User patient records, as stored in FHIR and associated with Groups that have imported patient data.
     * Produces a larger object containing all the properties required to populate My Details and My Conditions pages.
     * @param userId ID of User to retrieve patient record for
     * @param groupIds IDs of Groups to retrieve patient records from
     * @return List of Patient objects containing patient encounters, conditions etc
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/patient/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<org.patientview.api.model.Patient>> getPatientDetails(
            @PathVariable("userId") Long userId, @RequestParam(value = "groupId", required = false) List<Long> groupIds)
            throws FhirResourceException, ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(apiPatientService.get(userId, groupIds), HttpStatus.OK);
    }

    // API
    @ExcludeFromApiDoc
    @RequestMapping(value = "/patient/{userId}/group/{groupId}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void update(@PathVariable("userId") Long userId, @PathVariable(value = "groupId") Long groupId,
                       @RequestBody FhirPatient fhirPatient)
            throws FhirResourceException, ResourceForbiddenException, ResourceNotFoundException {
        apiPatientService.update(userId, groupId, fhirPatient);
    }
}
