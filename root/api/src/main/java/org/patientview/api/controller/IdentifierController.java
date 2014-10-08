package org.patientview.api.controller;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.IdentifierService;
import org.patientview.persistence.model.Identifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
@RestController
public class IdentifierController extends BaseController<IdentifierController> {

    @Inject
    private IdentifierService identifierService;

    @RequestMapping(value = "/identifier/{identifierId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("identifierId") Long identifierId) throws ResourceNotFoundException {
        identifierService.delete(identifierId);
    }

    @RequestMapping(value = "/identifier", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody Identifier identifier) throws ResourceNotFoundException, EntityExistsException {
        identifierService.saveIdentifier(identifier);
    }
}
