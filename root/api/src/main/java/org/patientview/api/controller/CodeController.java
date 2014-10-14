package org.patientview.api.controller;

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
 * Restful interface for the basic Crud operation for codes.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 26/06/2014
 */
@RestController
public class CodeController extends BaseController<CodeController> {

    @Inject
    private CodeService codeService;

    @RequestMapping(value = "/code", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Code> newCode(@RequestBody Code code)
        throws ResourceNotFoundException, EntityExistsException {
        return new ResponseEntity<>(codeService.add(code), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/code", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<Code>> getAllCodes(GetParameters getParameters) {
        return new ResponseEntity<>(codeService.getAllCodes(getParameters), HttpStatus.OK);
    }

    @RequestMapping(value = "/code/{codeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Code> getCode(@PathVariable("codeId") Long codeId) throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.get(codeId), HttpStatus.OK);
    }

    @RequestMapping(value = "/code", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Code> saveCode(@RequestBody Code code) throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.save(code), HttpStatus.OK);
    }

    @RequestMapping(value = "/code/{codeId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteCode(@PathVariable("codeId") Long codeId) {
        codeService.delete(codeId);
    }

    @RequestMapping(value = "/code/{codeId}/clone", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Code> cloneCode(@PathVariable("codeId") Long codeId) {
        return new ResponseEntity<>(codeService.cloneCode(codeId), HttpStatus.CREATED);
    }
}
