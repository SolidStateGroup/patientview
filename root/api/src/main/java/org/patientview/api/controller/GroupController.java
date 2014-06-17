package org.patientview.api.controller;

import org.patientview.api.service.AdminService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@RestController
public class GroupController extends BaseController {

    private final static Logger LOG = LoggerFactory.getLogger(GroupController.class);

    @Inject
    private AdminService adminService;

    @RequestMapping(value = "/group", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> createGroup(@RequestBody Group group, UriComponentsBuilder uriComponentsBuilder) {

        group = adminService.createGroup(group);

        LOG.info("Created group with id " + group.getId());
        UriComponents uriComponents = uriComponentsBuilder.path("/group/{id}").buildAndExpand(group.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Group>(group, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/group/{groupId}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Group> getGroup(@PathVariable("groupId") Long groupId) {
        return new ResponseEntity<Group>(adminService.getGroup(groupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/group", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Group>> getGroups() {
        return new ResponseEntity<List<Group>>(adminService.getAllGroups(), HttpStatus.OK);
    }


    @RequestMapping(value = "/group/{groupId}/feature/{featureId}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<GroupFeature> getGroup(@PathVariable("groupId") Long groupId,
                                          @PathVariable("featureId") Long featureId) {
        return new ResponseEntity<GroupFeature>(adminService.addGroupFeature(groupId, featureId), HttpStatus.OK);
    }

    @RequestMapping(value = "/groupfeature", method = RequestMethod.POST)
    public ResponseEntity<GroupFeature> createGroup(@RequestBody GroupFeature groupFeature, UriComponentsBuilder uriComponentsBuilder) {
        groupFeature = adminService.createGroupFeature(groupFeature);
        UriComponents uriComponents = uriComponentsBuilder.path("/group/{id}").buildAndExpand(groupFeature.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<GroupFeature>(groupFeature, HttpStatus.CREATED);
    }


}
