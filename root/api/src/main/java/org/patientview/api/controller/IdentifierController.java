package org.patientview.api.controller;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.IdentifierService;
import org.patientview.persistence.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
 * Created on 04/08/2014
 */
@RestController
public class IdentifierController {

    private final static Logger LOG = LoggerFactory.getLogger(IdentifierController.class);

    @Inject
    private IdentifierService identifierService;

    @RequestMapping(value = "/identifier/{identifierId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> delete(@PathVariable("identifierId") Long identifierId) {
        identifierService.delete(identifierId);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/identifier", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> save(@RequestBody Identifier identifier, UriComponentsBuilder uriComponentsBuilder)
        throws ResourceNotFoundException{
        Identifier updatedIdentifier = identifierService.save(identifier);
        LOG.info("Updated identifier with id " + updatedIdentifier.getId());
        UriComponents uriComponents = uriComponentsBuilder.path("/identifier/{identifierId}").buildAndExpand(updatedIdentifier.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.OK);
    }
}
