package org.patientview.api.controller;

import org.patientview.api.service.LinkService;
import org.patientview.persistence.model.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class LinkController {

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
}
