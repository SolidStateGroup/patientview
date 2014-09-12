package org.patientview.api.controller;

import org.patientview.api.service.ObservationHeadingService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.springframework.data.domain.Page;
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

/**
 * Restful interface for the basic Crud operation for observation (result) headings.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@RestController
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
    public ResponseEntity<Void> save(@RequestBody ObservationHeading observationHeading)
            throws ResourceNotFoundException  {
        try {
            observationHeadingService.save(observationHeading);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(value = "/observationheading/{observationHeadingId}/group/{groupId}", method = RequestMethod.POST)
    @ResponseBody
    public void addOrUpdateGroup(@PathVariable("observationHeadingId") Long observationHeadingId,
            @PathVariable("groupId") Long groupId, @RequestParam Long panel, @RequestParam Long panelOrder)
            throws ResourceNotFoundException {
        observationHeadingService.addOrUpdateGroup(observationHeadingId, groupId, panel, panelOrder);
    }

    @RequestMapping(value = "/observationheading/{observationHeadingId}/group/{groupId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeGroup(@PathVariable("observationHeadingId") Long observationHeadingId,
            @PathVariable("groupId") Long groupId) throws ResourceNotFoundException {
        observationHeadingService.removeGroup(observationHeadingId, groupId);
    }
}
