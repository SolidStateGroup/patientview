package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.CodeService;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Link;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;

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
    public ResponseEntity<Code> newCode(@RequestBody Code code, UriComponentsBuilder uriComponentsBuilder)
        throws ResourceNotFoundException {

        // create new code
        code = codeService.add(code);
        LOG.info("Created new code with id " + code.getId());

        // set header with location
        UriComponents uriComponents = uriComponentsBuilder.path("/code/{id}").buildAndExpand(code.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        // return created code
        return new ResponseEntity<>(code, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/code", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<Code>> getAllCodes(
            @RequestParam(value = "codeTypes", required = false) String[] codeTypes,
            @RequestParam(value = "filterText", required = false) String filterText,
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "size", required = false) String size,
            @RequestParam(value = "sortField", required = false) String sortField,
            @RequestParam(value = "sortDirection", required = false) String sortDirection,
            @RequestParam(value = "standardTypes", required = false) String[] standardTypes) {

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortDirection)) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection.equals("DESC")) {
                direction = Sort.Direction.DESC;
            }

            pageable = new PageRequest(pageConverted, sizeConverted, new Sort(new Sort.Order(direction, sortField)));
        } else {
            pageable = new PageRequest(pageConverted, sizeConverted);
        }

        return new ResponseEntity<>(codeService.getAllCodes(pageable, filterText, codeTypes, standardTypes)
                , HttpStatus.OK);
    }

    @RequestMapping(value = "/code/{codeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Code> getCode(@PathVariable("codeId") Long codeId) throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.get(codeId), HttpStatus.OK);
    }

    @RequestMapping(value = "/code", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Code> saveCode(@RequestBody Code code, UriComponentsBuilder uriComponentsBuilder)
            throws ResourceNotFoundException {
        LOG.info("Updated code with id " + code.getId());
        UriComponents uriComponents = uriComponentsBuilder.path("/code/{id}").buildAndExpand(code.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(codeService.save(code), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/code/{codeId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteCode(@PathVariable("codeId") Long codeId) {
        codeService.delete(codeId);
        return new ResponseEntity<>( HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/code/{codeId}/clone", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Code> cloneCode(@PathVariable("codeId") Long codeId
            , UriComponentsBuilder uriComponentsBuilder) {

        // create new code
        Code code = codeService.cloneCode(codeId);
        LOG.info("Cloned code with id " + codeId + " to create new code with id " + code.getId());

        // set header with location
        UriComponents uriComponents = uriComponentsBuilder.path("/code/{id}").buildAndExpand(code.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        // return created code
        return new ResponseEntity<>(code, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/code/{codeId}/links", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Link> addLink(@PathVariable("codeId") Long codeId, @RequestBody Link link
            , UriComponentsBuilder uriComponentsBuilder) {

        // create new link
        Link newLink = codeService.addLink(codeId, link);
        LOG.info("Created new Link with id " + newLink.getId() + " and added to Code with id " + codeId);

        // set header with location
        UriComponents uriComponents = uriComponentsBuilder.path("/link/{linkId}").buildAndExpand(newLink.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<>(newLink, HttpStatus.CREATED);
    }
}
