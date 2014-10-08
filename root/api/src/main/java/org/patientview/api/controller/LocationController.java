package org.patientview.api.controller;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.LocationService;
import org.patientview.persistence.model.Location;
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

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
public class LocationController extends BaseController<LocationController> {

    @Inject
    private LocationService locationService;

    @RequestMapping(value = "/location", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Location> createLocation(@RequestBody Location location) {
        return new ResponseEntity<>(locationService.add(location), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/location/{locationId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteLocation(@PathVariable("locationId") Long locationId) {
        locationService.delete(locationId);
    }

    @RequestMapping(value = "/location", method = RequestMethod.PUT)
    @ResponseBody
    public void saveLocation(@RequestBody Location location) throws ResourceNotFoundException {
        locationService.save(location);
    }
}
