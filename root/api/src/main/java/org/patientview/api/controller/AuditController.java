package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.AuditService;
import org.patientview.api.model.Audit;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * RESTful interface for retrieving Audit (log) information.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 12/11/2014
 */
@RestController
@ExcludeFromApiDoc
public class AuditController extends BaseController<AuditController> {

    @Inject
    private AuditService auditService;

    /**
     * Gets a Page of Audit information, with pagination parameters passed in as GetParameters
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size 
     * of page etc
     * @return Page containing a number of Audit objects, each of which has a Date, Action etc
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/audit", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Page<Audit>> findAll(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(auditService.findAll(getParameters), HttpStatus.OK);
    }
}
