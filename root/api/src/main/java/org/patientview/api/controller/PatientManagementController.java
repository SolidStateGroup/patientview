package org.patientview.api.controller;

import org.patientview.api.model.LookupType;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.PatientManagementService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.PatientManagement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * RESTful interface for retrieving the patient management records associated with a User, retrieved from FHIR.
 *
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
@RestController
public class PatientManagementController extends BaseController<PatientManagementController> {

    @Inject
    private CodeService codeService;

    @Inject
    private LookupService lookupService;

    @Inject
    private PatientManagementService patientManagementService;

    @RequestMapping(value = "/patientmanagement/{userId}/group/{groupId}/identifier/{identifierId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<PatientManagement> getPatientManagement(@PathVariable("userId") Long userId,
            @PathVariable("groupId") Long groupId, @PathVariable("identifierId") Long identifierId)
            throws ResourceNotFoundException, FhirResourceException {
        return new ResponseEntity<>(patientManagementService.get(userId, groupId, identifierId), HttpStatus.OK);
    }

    @RequestMapping(value = "/patientmanagement/diagnoses", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Code>> getPatientManagementDiagnoses() {
        return new ResponseEntity<>(codeService.getPatientManagementDiagnoses(), HttpStatus.OK);
    }

    @RequestMapping(value = "/patientmanagement/lookuptypes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<LookupType>> getPatientManagementLookupTypes() {
        return new ResponseEntity<>(lookupService.getPatientManagementLookupTypes(), HttpStatus.OK);
    }
}
