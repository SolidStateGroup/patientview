package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ResearchService;
import org.patientview.api.service.StaticDataManager;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ResearchStudy;
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
 * RESTful interface for the management and retrieval of News. NewsItems are made visible to specific Groups, Roles and
 * combinations of the two using NewsLinks. NewsItems can be made publicly available where they will appear on the home
 * page without logging in.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@RestController
@ExcludeFromApiDoc
public class RearchStudyController extends BaseController<RearchStudyController> {

    @Inject
    private ResearchService researchService;

    @Inject
    private StaticDataManager staticDataManager;

    /**
     * Add a NewsItem.
     * @param researchStudy Research Study to add
     * @return Long ID of the newly added NewsItem
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/research", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> add(@RequestBody ResearchStudy researchStudy)
    throws ResourceNotFoundException {
        return new ResponseEntity<>(researchService.add(researchStudy), HttpStatus.CREATED);
    }


    /**
     * Get a single NewsItem.
     * @return ResearchStudy object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/research", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Page<ResearchStudy>> get()
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(researchService.getAll(), HttpStatus.OK);
    }

    /**
     * Update a NewsItem.
     * @param researchStudy Research Study to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/research", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody ResearchStudy researchStudy) throws ResourceNotFoundException, ResourceForbiddenException {
        researchService.save(researchStudy);
    }

    /**
     * Get a Page of NewsItems for a specific User.
     * @param userId ID of User to retrieve news for
     * @param size Size of the page
     * @param newsTypeString the id of the items we want to show
     * @param limitResults if we want to show all items or just 2 items per group (dashboard only)
     * @param page Page number
     * @return Page of NewsItem for a specific User
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/research", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<ResearchStudy>> findByUserId(
            @PathVariable("userId") Long userId, @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "newsType", required = false) String newsTypeString,
            @RequestParam(value = "limitResults", required = false) boolean limitResults,
            @RequestParam(value = "page", required = false) String page) throws ResourceNotFoundException,
            ResourceForbiddenException, FhirResourceException {
        return new ResponseEntity<>(researchService.getAllForUser(userId), HttpStatus.OK);
    }
}
