package org.patientview.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.NhsIndicators;
import org.patientview.api.service.NhsIndicatorsService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
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
 * RESTful interface for managing Nhs Indicators.
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@RestController
@ExcludeFromApiDoc
public class NhsIndicatorsController extends BaseController<NhsIndicatorsController> {

    @Inject
    private NhsIndicatorsService nhsIndicatorsService;

    /**
     * Get NHS Indicators for a single Group.
     * @param groupId ID of Group to get NHS Indicators for
     * @return Group object
     * @throws ResourceNotFoundException group not found
     * @throws ResourceForbiddenException forbidden
     * @throws FhirResourceException thrown when error retrieving from FHIR
     */
    @RequestMapping(value = "/group/{groupId}/nhsindicators", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<NhsIndicators> getNhsIndicators(@PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        return new ResponseEntity<>(nhsIndicatorsService.getNhsIndicators(groupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/nhsindicators/all", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<NhsIndicators>> getAllNhsIndicators()
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException,
                JsonProcessingException {
        return new ResponseEntity<>(nhsIndicatorsService.getAllNhsIndicatorsAndStore(true), HttpStatus.OK);
    }
}
