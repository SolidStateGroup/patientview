package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.Pathway;
import org.patientview.api.service.PathwayService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * RESTful interface for Pathway.
 */
@RestController
@ExcludeFromApiDoc
public class PathwayController extends BaseController<PathwayController> {

    @Inject
    private PathwayService pathwayService;

    /**
     * Get a Pathway for a user.
     *
     * @param userId      ID of User to retrieve
     * @param pathwayType an id of the Note to retrieve
     * @return a Pathway object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/pathway/{pathwayType}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Pathway> getPathway(@PathVariable("userId") Long userId,
                                              @PathVariable("pathwayType") PathwayTypes pathwayType)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(pathwayService.getPathway(userId, pathwayType), HttpStatus.OK);
    }

    /**
     * Update Note for a user.
     *
     * @param userId  ID of User to update the Pathway  for
     * @param pathway a Pathway containing updated properties
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/pathway", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updatePathway(@PathVariable("userId") Long userId, @RequestBody Pathway pathway)
            throws ResourceNotFoundException, ResourceForbiddenException {
        pathwayService.updatePathway(userId, pathway);
    }
}
