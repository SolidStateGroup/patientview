package org.patientview.importer.controller;

import generated.Patientview;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.service.ImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Entry point for the importer
 *
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 */
@RestController
public class ImportController {

    private final static Logger LOG = LoggerFactory.getLogger(ImportController.class);

    @Inject
    ImportService importService;

    @PostConstruct
    public void init() {
        LOG.info("Import Controller Started");
    }

    @RequestMapping(value = "/import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<Void> importPatient(@RequestBody Patientview patientview) throws ImportResourceException {
        importService.importRecord(patientview);
        return new ResponseEntity<>(HttpStatus.OK);

    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getTest() throws ImportResourceException {

        return new ResponseEntity<>("Importer OK", HttpStatus.OK);

    }

}
