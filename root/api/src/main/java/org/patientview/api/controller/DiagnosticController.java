package org.patientview.api.controller;

import org.patientview.api.model.FhirDiagnosticReport;
import org.patientview.api.service.DiagnosticService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
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
 * RESTful interface for Diagnostics, not currently used heavily but found in UI from patient results page.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/10/2014
 */
@RestController
public class DiagnosticController extends BaseController<DiagnosticController> {

    @Inject
    private DiagnosticService diagnosticService;

    /**
     * Get Diagnostics for a patient from FHIR given a User ID.
     * @param userId ID of User to get Diagnostics for
     * @return List of diagnostic reports, retrieved from FHIR
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/diagnostics", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirDiagnosticReport>> getAllDiagnostics(@PathVariable("userId") Long userId)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(diagnosticService.getByUserId(userId), HttpStatus.OK);
    }
}
