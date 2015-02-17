package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.LocationService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
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
 * RESTful interface for management of Locations, attached to Groups.
 *  
 * Created by jamesr@solidstategroup.com
 * Created on 30/07/2014
 */
@RestController
@ExcludeFromApiDoc
public class LocationController extends BaseController<LocationController> {

    @Inject
    private LocationService locationService;

    /**
     * Add a Location to a Group. 
     * @param groupId ID of Group to add Location to
     * @param location Location to add
     * @return Location object, newly created (note: consider just returning ID)
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/group/{groupId}/locations", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Location> add(@PathVariable("groupId") Long groupId,
                                                @RequestBody Location location)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(locationService.add(groupId, location), HttpStatus.CREATED);
    }

    /**
     * Delete a Location. 
     * @param locationId ID of Location to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/location/{locationId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("locationId") Long locationId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        locationService.delete(locationId);
    }

    /**
     * Update a Location.
     * @param location Location object to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/location", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody Location location)
            throws ResourceNotFoundException, ResourceForbiddenException {
        locationService.save(location);
    }
}
