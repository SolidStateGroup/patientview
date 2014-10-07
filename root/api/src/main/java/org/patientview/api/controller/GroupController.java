package org.patientview.api.controller;

import org.patientview.api.model.GroupStatisticTO;
import org.patientview.api.model.UnitRequest;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.GroupStatisticService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@RestController
public class GroupController extends BaseController<GroupController> {

    @Inject
    private GroupService groupService;

    @Inject
    private GroupStatisticService groupStatisticService;

    @RequestMapping(value = "/group", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createGroup(@RequestBody Group group) {
        groupService.add(group);
    }

    @RequestMapping(value = "/public/group", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<org.patientview.api.model.Group>> getGroupsPublic() {
        return new ResponseEntity<>(groupService.findAllPublic(), HttpStatus.OK);
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
    public void saveGroup(@RequestBody Group group) throws ResourceNotFoundException{
        groupService.save(group);
    }

    @RequestMapping(value = "/group/type/{typeId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Group>> getGroupsByType(@PathVariable("typeId") Long lookupId) {
        return new ResponseEntity<>(groupService.findGroupByType(lookupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/parent/{parentId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addParentGroup(@PathVariable("groupId") Long groupId,
                                                      @PathVariable("parentId") Long parentGroupId) {
        groupService.addParentGroup(groupId,parentGroupId);
    }

    @RequestMapping(value = "/group/{groupId}/parent/{parentId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteParentGroup(@PathVariable("groupId") Long groupId, @PathVariable("parentId") Long parentGroupId) {
        groupService.deleteParentGroup(groupId, parentGroupId);
    }

    @RequestMapping(value = "/group/{groupId}/children", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Group>> getChildren(@PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException{
        return new ResponseEntity<>(groupService.findChildren(groupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/child/{childId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addChildGroup(@PathVariable("groupId") Long groupId, @PathVariable("childId") Long childGroupId) {
        groupService.addChildGroup(groupId,childGroupId);
    }

    @RequestMapping(value = "/group/{groupId}/child/{childId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteChildGroup(@PathVariable("groupId") Long groupId, @PathVariable("childId") Long childGroupId) {
        groupService.deleteChildGroup(groupId, childGroupId);
    }

    @RequestMapping(value = "/group/{groupId}/links", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Link> addLink(@PathVariable("groupId") Long groupId, @RequestBody Link link) {
        return new ResponseEntity<>(groupService.addLink(groupId, link), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/group/{groupId}/contactpoints", method = RequestMethod.POST,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ContactPoint> addContactPoint(@PathVariable("groupId") Long groupId,
                                                        @RequestBody ContactPoint contactPoint) {
        return new ResponseEntity<>(groupService.addContactPoint(groupId, contactPoint), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/group/{groupId}/locations", method = RequestMethod.POST,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Location> addLocation(@PathVariable("groupId") Long groupId, @RequestBody Location location) {
        return new ResponseEntity<>(groupService.addLocation(groupId, location), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/group/{groupId}/features/{featureId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addFeature(@PathVariable("groupId") Long groupId, @PathVariable("featureId") Long featureId) {
        groupService.addFeature(groupId, featureId);
    }

    @RequestMapping(value = "/group/{groupId}/features/{featureId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteFeature(@PathVariable("groupId") Long groupId, @PathVariable("featureId") Long featureId) {
        groupService.deleteFeature(groupId, featureId);
    }

    @RequestMapping(value = "/group/{groupId}/statistics", method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Collection<GroupStatisticTO>> getStatistics(@PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException {
        Collection<GroupStatisticTO> groupStatisticTO
            = Util.convertGroupStatistics(groupStatisticService.getMonthlyGroupStatistics(groupId));
        return new ResponseEntity<>(groupStatisticTO, HttpStatus.OK);
    }

    @RequestMapping(value = "/group/{groupId}/contactunit", method = RequestMethod.POST)
    @ResponseBody
    public void contactUnit(@PathVariable("groupId") Long groupId, @RequestBody UnitRequest unitRequest)
            throws ResourceNotFoundException, ResourceInvalidException {
        groupService.contactUnit(groupId, unitRequest);
    }
}
