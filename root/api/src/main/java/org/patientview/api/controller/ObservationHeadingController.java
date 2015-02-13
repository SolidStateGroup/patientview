package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.ObservationHeadingGroup;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ResultCluster;
import org.springframework.data.domain.Page;
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
 * Restful interface for the basic Crud operation for observation (result) headings.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@RestController
@ExcludeFromApiDoc
public class ObservationHeadingController extends BaseController<ObservationHeadingController> {

    @Inject
    private ObservationHeadingService observationHeadingService;

    @RequestMapping(value = "/observationheading", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<ObservationHeading>> findAll(GetParameters getParameters) {
        return new ResponseEntity<>(observationHeadingService.findAll(getParameters), HttpStatus.OK);
    }

    @RequestMapping(value = "/observationheading/{observationHeadingId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ObservationHeading> get(@PathVariable("observationHeadingId") Long observationHeadingId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(observationHeadingService.get(observationHeadingId), HttpStatus.OK);
    }

    @RequestMapping(value = "/observationheading", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ObservationHeading> add(@RequestBody ObservationHeading observationHeading) {
        return new ResponseEntity<>(observationHeadingService.add(observationHeading), HttpStatus.OK);
    }

    @RequestMapping(value = "/observationheading", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody ObservationHeading observationHeading) throws ResourceNotFoundException  {
        observationHeadingService.save(observationHeading);
    }

    @RequestMapping(
            value = "/observationheading/{observationHeadingId}/group/{groupId}/panel/{panel}/panelorder/{panelOrder}",
            method = RequestMethod.POST)
    @ResponseBody
    public void addObservationHeadingGroup(@PathVariable("observationHeadingId") Long observationHeadingId,
            @PathVariable("groupId") Long groupId, @PathVariable("panel") Long panel,
            @PathVariable("panelOrder") Long panelOrder) throws ResourceNotFoundException, ResourceForbiddenException {
        observationHeadingService.addObservationHeadingGroup(observationHeadingId, groupId, panel, panelOrder);
    }

    @RequestMapping(value = "/observationheadinggroup", method = RequestMethod.PUT)
    @ResponseBody
    public void updateObservationHeadingGroup(@RequestBody ObservationHeadingGroup observationHeadingGroup)
            throws ResourceNotFoundException, ResourceForbiddenException {
        observationHeadingService.updateObservationHeadingGroup(observationHeadingGroup);
    }

    @RequestMapping(value = "/observationheadinggroup/{observationHeadingGroupId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeObservationHeadingGroup(@PathVariable("observationHeadingGroupId") Long observationHeadingGroupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        observationHeadingService.removeObservationHeadingGroup(observationHeadingGroupId);
    }

    // ResultCluster for patient entered results
    @RequestMapping(value = "/resultclusters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ResultCluster>> getResultClusters() {
        return new ResponseEntity<>(observationHeadingService.getResultClusters(), HttpStatus.OK);
    }

    // Get available result types for user (where results are currently available)
    @RequestMapping(value = "/user/{userId}/availableobservationheadings", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ObservationHeading>> getAvailableObservationHeadings(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, FhirResourceException {
        return new ResponseEntity<>(observationHeadingService.getAvailableObservationHeadings(userId), HttpStatus.OK);
    }

    // Get saved result types for user (used in table view)
    @RequestMapping(value = "/user/{userId}/savedobservationheadings", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ObservationHeading>> getSavedObservationHeadings(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, FhirResourceException {
        return new ResponseEntity<>(observationHeadingService.getSavedObservationHeadings(userId), HttpStatus.OK);
    }

    // Store user's selection of observation headings (used in table view)
    @RequestMapping(value = "/user/{userId}/saveobservationheadingselection", method = RequestMethod.POST)
    @ResponseBody
    public void saveObservationHeadingSelection(@PathVariable("userId") Long userId, @RequestBody String[] codes)
            throws ResourceNotFoundException {
        observationHeadingService.saveObservationHeadingSelection(userId, codes);
    }

    // Get result types for user that can be used when setting up alerts
    @RequestMapping(value = "/user/{userId}/availablealertobservationheadings", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<ObservationHeading>> getAvailableAlertObservationHeadings(
            @PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(
                observationHeadingService.getAvailableAlertObservationHeadings(userId), HttpStatus.OK);
    }
}
