package org.patientview.api.controller;

import org.patientview.api.model.LookupType;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.PatientManagementService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.PatientManagement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
 * RESTful interface for retrieving/storing the patient management records associated with a User, retrieved from FHIR.
 * Used by IBD Patient Management.
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

    /**
     * Get a PatientManagement object containing observations, diagnosis etc for use in IBD Patient Management.
     * @param userId Long ID of User (patient)
     * @param groupId Long ID of Group
     * @param identifierId Long ID of Identifier
     * @return PatientManagement containing IBD Patient Management information
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RequestMapping(value = "/patientmanagement/{userId}/group/{groupId}/identifier/{identifierId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<PatientManagement> getPatientManagement(@PathVariable("userId") Long userId,
            @PathVariable("groupId") Long groupId, @PathVariable("identifierId") Long identifierId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        return new ResponseEntity<>(patientManagementService.get(userId, groupId, identifierId), HttpStatus.OK);
    }

    /**
     * Get a list of Code corresponding to IBD Patient Management Diagnoses, currently stored in property
     * "patient.management.diagnoses.codes" with CD, UC IBDU
     * @return List of Code
     */
    @RequestMapping(value = "/patientmanagement/diagnoses", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Code>> getPatientManagementDiagnoses() {
        return new ResponseEntity<>(codeService.getPatientManagementDiagnoses(), HttpStatus.OK);
    }

    /**
     * Get List of LookupType (and child Lookups) used by IBD Patient Management e.g. IBD_SURGERYMAINPROCEDURE,
     * IBD_SMOKINGSTATUS stored in LookupTypesPatientManagement. Used to populate select and multi-select in UI.
     * @return List of LookupType and child Lookups
     */
    @RequestMapping(value = "/patientmanagement/lookuptypes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<LookupType>> getPatientManagementLookupTypes() {
        return new ResponseEntity<>(lookupService.getPatientManagementLookupTypes(), HttpStatus.OK);
    }

    /**
     * Save PatientManagement information, for IBD Patient Management, used when saving from UI
     * @param userId Long ID of User (patient)
     * @param groupId Long ID of Group
     * @param identifierId Long ID of Identifier
     * @param patientManagement PatientManagement object containing observations, diagnosis etc
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RequestMapping(value = "/patientmanagement/{userId}/group/{groupId}/identifier/{identifierId}",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void savePatientManagement(@PathVariable("userId") Long userId,
            @PathVariable("groupId") Long groupId, @PathVariable("identifierId") Long identifierId,
            @RequestBody PatientManagement patientManagement)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        patientManagementService.save(userId, groupId, identifierId, patientManagement);
    }
}
