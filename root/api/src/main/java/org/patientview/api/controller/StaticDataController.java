package org.patientview.api.controller;

import org.patientview.api.service.StaticDataManager;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Lookup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@RestController
public class StaticDataController {

    @Inject
    private StaticDataManager staticDataManager;

    @RequestMapping(value = "/lookups", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Lookup>> getAllLookups() {

        return new ResponseEntity<List<Lookup>>(staticDataManager.getAllLookups(), HttpStatus.OK);
    }

    @RequestMapping(value = "/features", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Feature>> getAllFeatures() {
        return new ResponseEntity<List<Feature>>(staticDataManager.getAllFeatures(), HttpStatus.OK);
    }


}
