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
import java.io.IOException;
import java.util.Date;
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
     * Testing only: get all NHS Indicators.
     * @return List of all NHS Indicators
     * @throws ResourceNotFoundException thrown when lookup, group etc not found
     * @throws FhirResourceException thrown when retrieving data from FHIR
     * @throws JsonProcessingException thrown when converting data to JSON
     */
    @RequestMapping(value = "/nhsindicators/all", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<NhsIndicators>> getAllNhsIndicators()
            throws ResourceNotFoundException, FhirResourceException, JsonProcessingException {
        return new ResponseEntity<>(nhsIndicatorsService.getAllNhsIndicatorsAndStore(true), HttpStatus.OK);
    }

    /**
     * Get NHS Indicators for a single Group.
     * @param groupId ID of Group to get NHS Indicators for
     * @return Group object
     * @throws ResourceNotFoundException group not found
     * @throws ResourceForbiddenException forbidden
     * @throws FhirResourceException thrown when retrieving from FHIR
     */
    @RequestMapping(value = "/group/{groupId}/nhsindicators", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<NhsIndicators> getNhsIndicatorsByGroup(@PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        return new ResponseEntity<>(nhsIndicatorsService.getNhsIndicators(groupId), HttpStatus.OK);
    }

    /**
     * Get single NhsIndicators given group ID and date (as Long).
     * @param groupId Long ID of Group
     * @param date Long representation of Date
     * @return NhsIndicators
     * @throws ResourceNotFoundException Group or NhsIndicators not found
     * @throws ResourceForbiddenException thrown by security aspect
     * @throws IOException thrown when converting to transport object
     */
    @RequestMapping(value = "/group/{groupId}/nhsindicators/{date}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<NhsIndicators> getNhsIndicatorsByGroupAndDate(@PathVariable("groupId") Long groupId,
                                                                        @PathVariable("date") Long date)
            throws ResourceNotFoundException, ResourceForbiddenException, IOException {
        return new ResponseEntity<>(nhsIndicatorsService.getNhsIndicatorsByGroupAndDate(groupId, date), HttpStatus.OK);
    }

    /**
     * Get all NHS Indicator dates (when task was run).
     * @return List of Date
     */
    @RequestMapping(value = "/nhsindicators/dates", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Date>> getNhsIndicatorsDates() {
        return new ResponseEntity<>(nhsIndicatorsService.getNhsIndicatorsDates(), HttpStatus.OK);
    }
}
