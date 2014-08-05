package org.patientview.api.controller;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.LocationService;
import org.patientview.persistence.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
public class LocationController {

    private final static Logger LOG = LoggerFactory.getLogger(LocationController.class);

    @Inject
    private LocationService locationService;


    @RequestMapping(value = "/location", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Location> createLocation(@RequestBody Location location,
                                           UriComponentsBuilder uriComponentsBuilder) {

        location = locationService.add(location);

        UriComponents uriComponents = uriComponentsBuilder.path("/location/{id}").buildAndExpand(location.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Location>(location, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/location/{locationId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteLocation(@PathVariable("locationId") Long locationId) {
        locationService.delete(locationId);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/location", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> saveLocation(@RequestBody Location location, UriComponentsBuilder uriComponentsBuilder)
        throws ResourceNotFoundException {

        Location updatedLocation = locationService.save(location);
        LOG.info("Updated location with id " + updatedLocation.getId());
        UriComponents uriComponents = uriComponentsBuilder.path("/location/{locationId}").buildAndExpand(updatedLocation.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.OK);
    }
}
