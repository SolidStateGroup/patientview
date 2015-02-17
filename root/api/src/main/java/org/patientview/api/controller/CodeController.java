package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.CodeService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.GetParameters;
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
import javax.persistence.EntityExistsException;

/**
 * RESTful interface for the basic Crud operation for Codes (treatment and diagnosis).
 *
 * Created by jamesr@solidstategroup.com
 * Created on 26/06/2014
 */
@RestController
@ExcludeFromApiDoc
public class CodeController extends BaseController<CodeController> {

    @Inject
    private CodeService codeService;

    /**
     * Create a new Code.
     * @param code Code object containing all required properties
     * @return Code object, newly created (note: consider only returning ID or HTTP OK)
     * @throws ResourceNotFoundException
     * @throws EntityExistsException
     */
    @RequestMapping(value = "/code", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Code> add(@RequestBody Code code)
            throws ResourceNotFoundException, EntityExistsException {
        return new ResponseEntity<>(codeService.add(code), HttpStatus.CREATED);
    }

    /**
     * Make a copy of an existing Code, typically to avoid having to re-enter large amounts of similar information in
     * UI.
     * @param codeId ID of Code to clone
     * @return Code object, newly created based on another Code (note: consider only returning ID or HTTP OK)
     */
    @RequestMapping(value = "/code/{codeId}/clone", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Code> cloneCode(@PathVariable("codeId") Long codeId) {
        return new ResponseEntity<>(codeService.cloneCode(codeId), HttpStatus.CREATED);
    }

    /**
     * Delete a Code.
     * @param codeId ID of Code to delete
     */
    @RequestMapping(value = "/code/{codeId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("codeId") Long codeId) {
        codeService.delete(codeId);
    }

    /**
     * Get a Page of Code object, with pagination parameters (page, size of page etc) passed in as GetParameters object.
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size
     * of page etc
     * @return Page of Code objects
     */
    @RequestMapping(value = "/code", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<Code>> getAllCodes(GetParameters getParameters) {
        return new ResponseEntity<>(codeService.getAllCodes(getParameters), HttpStatus.OK);
    }

    /**
     * Get a single Code given an ID.
     * @param codeId ID of Code to retrieve
     * @return Code object
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/code/{codeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Code> get(@PathVariable("codeId") Long codeId) throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.get(codeId), HttpStatus.OK);
    }

    /**
     * Update an existing Code.
     * @param code Code object with updated properties
     * @return Code object, updated (note: consider only returning ID or HTTP OK)
     * @throws ResourceNotFoundException
     * @throws EntityExistsException
     */
    @RequestMapping(value = "/code", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Code> save(@RequestBody Code code) throws ResourceNotFoundException, EntityExistsException {
        return new ResponseEntity<>(codeService.save(code), HttpStatus.OK);
    }
}
