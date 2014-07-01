package org.patientview.api.controller;

import org.patientview.api.service.StaticDataManager;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Lookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@RestController
public class StaticDataController extends  BaseController {

    private final static Logger LOG = LoggerFactory.getLogger(SecurityController.class);

    @Inject
    private StaticDataManager staticDataManager;

    @RequestMapping(value = "/lookup", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Lookup>> getAllLookups() {

        return new ResponseEntity<List<Lookup>>(staticDataManager.getAllLookups(), HttpStatus.OK);
    }

    // get lookups by lookupType type string
    @RequestMapping(value = "/lookupType/{lookupType}/lookups", method = RequestMethod.GET
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Lookup>> getLookupsByType(@PathVariable("lookupType") String lookupType) {
        LOG.debug("Request has been received to get lookups by type: {}", lookupType);
        return new ResponseEntity<List<Lookup>>(staticDataManager.getLookupsByType(lookupType), HttpStatus.OK);
    }

    @RequestMapping(value = "/feature", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Feature>> getAllFeatures(
            @RequestParam(value = "type", required = false) String featureType, HttpServletRequest request) {

        if (!request.getParameterMap().containsKey("type")) {
            return new ResponseEntity<List<Feature>>(staticDataManager.getAllFeatures(), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<Feature>>(staticDataManager.getFeaturesByType(featureType), HttpStatus.OK);
        }
    }
}
