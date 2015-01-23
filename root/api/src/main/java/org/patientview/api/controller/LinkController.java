package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.LinkService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Link;
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
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
@RestController
@ExcludeFromApiDoc
public class LinkController extends BaseController<LinkController> {

    @Inject
    private LinkService linkService;

    @RequestMapping(value = "/link/{linkId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("linkId") Long linkId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        linkService.delete(linkId);
    }

    @RequestMapping(value = "/link", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody Link link) throws ResourceNotFoundException, ResourceForbiddenException {
        linkService.save(link);
    }

    @RequestMapping(value = "/group/{groupId}/links", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Link> addGroupLink(@PathVariable("groupId") Long groupId, @RequestBody Link link)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(linkService.addGroupLink(groupId, link), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/code/{codeId}/links", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Link> addCodeLink(@PathVariable("codeId") Long codeId, @RequestBody Link link)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(linkService.addCodeLink(codeId, link), HttpStatus.CREATED);
    }
}
