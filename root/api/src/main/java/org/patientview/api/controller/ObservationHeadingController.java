package org.patientview.api.controller;

import org.patientview.api.service.ObservationHeadingService;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.ObservationHeading;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * Restful interface for the basic Crud operation for observation (result) headings.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@RestController
public class ObservationHeadingController extends BaseController<ObservationHeadingController> {

    @Inject
    private ObservationHeadingService observationHeadingService;

    @RequestMapping(value = "/observationheading", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<ObservationHeading>> getAllCodes(GetParameters getParameters) {
        return new ResponseEntity<>(observationHeadingService.findAll(getParameters), HttpStatus.OK);
    }
}
