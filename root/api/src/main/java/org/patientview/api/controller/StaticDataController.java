package org.patientview.api.controller;

import org.patientview.api.service.StaticDataManager;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.Lookup;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

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

    @RequestMapping(value = "/groupfeature", method = RequestMethod.POST)
    public ResponseEntity<GroupFeature> createGroup(@RequestBody GroupFeature groupFeature, UriComponentsBuilder uriComponentsBuilder) {
        groupFeature = staticDataManager.createGroupFeature(groupFeature);
        UriComponents uriComponents = uriComponentsBuilder.path("/group/{id}").buildAndExpand(groupFeature.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<GroupFeature>(groupFeature, HttpStatus.CREATED);

    }

}
