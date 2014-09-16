package org.patientview.api.controller;

import org.patientview.api.model.GroupStatisticTO;
import org.patientview.api.model.UnitRequest;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.AdminService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.GroupStatisticService;
import org.patientview.api.service.JoinRequestService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.User;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@RestController
public class GroupController extends BaseController<GroupController> {

    private final static Logger LOG = LoggerFactory.getLogger(GroupController.class);

    @Inject
    private JoinRequestService joinRequestService;

    @Inject
    private AdminService adminService;

    @Inject
    private GroupService groupService;

    @Inject
    private GroupStatisticService groupStatisticService;

    @RequestMapping(value = "/group", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Group> createGroup(@RequestBody Group group, UriComponentsBuilder uriComponentsBuilder) {

        group = groupService.add(group);

        LOG.info("Created group with id " + group.getId());
        UriComponents uriComponents = uriComponentsBuilder.path("/group/{id}").buildAndExpand(group.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(group, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/group", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Group>> getGroups() {
        return new ResponseEntity<>(groupService.findAll(), HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Group> getGroup(@PathVariable("groupId") Long groupId) throws SecurityException {
        return new ResponseEntity<>(groupService.get(groupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/group", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> saveGroup(@RequestBody Group group, UriComponentsBuilder uriComponentsBuilder) {

        try {
            group = groupService.save(group);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        LOG.info("Updated group with id " + group.getId());
        UriComponents uriComponents = uriComponentsBuilder.path("/group/{id}").buildAndExpand(group.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }


    @RequestMapping(value = "/group/{groupId}/feature/{featureId}", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<GroupFeature> getGroup(@PathVariable("groupId") Long groupId,
                                          @PathVariable("featureId") Long featureId) {
        return new ResponseEntity<>(adminService.addGroupFeature(groupId, featureId), HttpStatus.OK);
    }

    @RequestMapping(value = "/groupfeature", method = RequestMethod.POST)
    public ResponseEntity<GroupFeature> createGroupFeature(@RequestBody GroupFeature groupFeature, UriComponentsBuilder uriComponentsBuilder) {
        groupFeature = adminService.createGroupFeature(groupFeature);
        UriComponents uriComponents = uriComponentsBuilder.path("/group/{id}").buildAndExpand(groupFeature.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(groupFeature, HttpStatus.CREATED);
    }

    //TODO, similar to /user?roleType=staff&groupId=111&groupId=222&groupId=333 in UserController.java but only for a single group
    @RequestMapping(value = "/group/{groupId}/user", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<User>> getGroupStaff(@PathVariable("groupId") Long groupId,
                                                    @RequestParam("roleType") String roleType) {
        return new ResponseEntity<>(adminService.getGroupUserByRoleStaff(groupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/group/type/{typeId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Group>> getGroupsByType(@PathVariable("typeId") Long lookupId) {
        return new ResponseEntity<>(groupService.findGroupByType(lookupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/parent/{parentId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> addParentGroup(@PathVariable("groupId") Long groupId,
                                                      @PathVariable("parentId") Long parentGroupId) {
        groupService.addParentGroup(groupId,parentGroupId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/parent/{parentId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteParentGroup(@PathVariable("groupId") Long groupId,
                                                  @PathVariable("parentId") Long parentGroupId) {
        groupService.deleteParentGroup(groupId, parentGroupId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/joinRequest", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> addJoinRequest(@PathVariable("groupId") Long groupId,
                                               @RequestBody JoinRequest joinRequest) throws ResourceNotFoundException {
        LOG.debug("Join Request Received for {} {}", joinRequest.getForename(), joinRequest.getSurname());

        joinRequestService.add(groupId, joinRequest);

        return new ResponseEntity<>(HttpStatus.CREATED);

    }

    @RequestMapping(value = "/group/{groupId}/children", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Group>> getChildren(@PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException{
        return new ResponseEntity<>(groupService.findChildren(groupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/child/{childId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> addChildGroup(@PathVariable("groupId") Long groupId,
                                                      @PathVariable("childId") Long childGroupId) {
        groupService.addChildGroup(groupId,childGroupId);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/child/{childId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteChildGroup(@PathVariable("groupId") Long groupId,
                                                  @PathVariable("childId") Long childGroupId) {
        groupService.deleteChildGroup(groupId, childGroupId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/links", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Link> addLink(@PathVariable("groupId") Long groupId, @RequestBody Link link
            , UriComponentsBuilder uriComponentsBuilder) {

        // create new link
        Link newLink = groupService.addLink(groupId, link);
        LOG.info("Created new Link with id " + newLink.getId() + " and added to Group with id " + groupId);

        // set header with location
        UriComponents uriComponents = uriComponentsBuilder.path("/link/{linkId}").buildAndExpand(newLink.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<>(newLink, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/group/{groupId}/contactpoints", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ContactPoint> addContactPoint(@PathVariable("groupId") Long groupId, @RequestBody ContactPoint contactPoint
            , UriComponentsBuilder uriComponentsBuilder) {

        // create new contactPoint
        ContactPoint newContactPoint = groupService.addContactPoint(groupId, contactPoint);
        LOG.info("Created new ContactPoint with id " + newContactPoint.getId() + " and added to Group with id " + groupId);

        // set header with location
        UriComponents uriComponents = uriComponentsBuilder.path("/contactpoint/{contactPointId}").buildAndExpand(newContactPoint.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<>(newContactPoint, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/group/{groupId}/locations", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Location> addLocation(@PathVariable("groupId") Long groupId, @RequestBody Location location
            , UriComponentsBuilder uriComponentsBuilder) {

        // create new location
        Location newLocation = groupService.addLocation(groupId, location);
        LOG.info("Created new Location with id " + newLocation.getId() + " and added to Group with id " + groupId);

        // set header with location
        UriComponents uriComponents = uriComponentsBuilder.path("/location/{locationId}").buildAndExpand(newLocation.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<>(newLocation, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/group/{groupId}/features/{featureId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> addFeature(@PathVariable("groupId") Long groupId,
                                               @PathVariable("featureId") Long featureId) {
        groupService.addFeature(groupId, featureId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/features/{featureId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteFeature(@PathVariable("groupId") Long groupId,
                                               @PathVariable("featureId") Long featureId) {
        groupService.deleteFeature(groupId, featureId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/group/{groupId}/statistics", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Collection<GroupStatisticTO>> getStatistics(@PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException {
        Collection<GroupStatisticTO> groupStatisticTO = Util.convertGroupStatistics(groupStatisticService.getMonthlyGroupStatistics(groupId));
        return new ResponseEntity<>(groupStatisticTO, HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/contactunit", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Void> contactUnit(@PathVariable("groupId") Long groupId,
                                            @RequestBody UnitRequest unitRequest)
            throws ResourceNotFoundException, ResourceInvalidException {
        groupService.contactUnit(groupId, unitRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
