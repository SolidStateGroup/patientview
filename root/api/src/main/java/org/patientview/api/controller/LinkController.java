package org.patientview.api.controller;

import org.patientview.api.service.LinkService;
import org.patientview.persistence.model.Link;
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
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
@RestController
public class LinkController extends BaseController<LinkController> {

    @Inject
    private LinkService linkService;


    @RequestMapping(value = "/link", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Link> createLink(@RequestBody Link link,
                                           UriComponentsBuilder uriComponentsBuilder) {

        link = linkService.create(link);

        UriComponents uriComponents = uriComponentsBuilder.path("/link/{id}").buildAndExpand(link.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Link>(link, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/link/{linkId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteLink(@PathVariable("linkId") Long linkId) {
        linkService.deleteLink(linkId);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/link", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> saveLink(@RequestBody Link link, UriComponentsBuilder uriComponentsBuilder) {
        Link updatedLink = linkService.saveLink(link);
        LOG.info("Updated link with id " + updatedLink.getId());
        UriComponents uriComponents = uriComponentsBuilder.path("/link/{linkId}").buildAndExpand(updatedLink.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.OK);
    }
}
