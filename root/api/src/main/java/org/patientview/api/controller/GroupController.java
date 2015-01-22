package org.patientview.api.controller;

import org.patientview.api.model.BaseGroup;
import org.patientview.api.model.GroupStatisticTO;
import org.patientview.api.model.UnitRequest;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.GroupStatisticService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupStatistic;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.mail.MessagingException;
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
    public ResponseEntity<Long> createGroup(@RequestBody Group group) {
        return new ResponseEntity<>(groupService.add(group), HttpStatus.OK);
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
    public ResponseEntity<Group> getGroup(@PathVariable("groupId") Long groupId)
            throws SecurityException, ResourceForbiddenException {
        return new ResponseEntity<>(groupService.get(groupId), HttpStatus.OK);
    }

    @RequestMapping(value = "/group", method = RequestMethod.PUT)
    @ResponseBody
    public void saveGroup(@RequestBody Group group) throws ResourceNotFoundException, ResourceForbiddenException {
        groupService.save(group);
    }

    @RequestMapping(value = "/group/{groupId}/parent/{parentId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addParentGroup(@PathVariable("groupId") Long groupId,
                                                      @PathVariable("parentId") Long parentGroupId) {
        groupService.addParentGroup(groupId, parentGroupId);
    }

    @RequestMapping(value = "/group/{groupId}/parent/{parentId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteParentGroup(@PathVariable("groupId") Long groupId, @PathVariable("parentId") Long parentGroupId) {
        groupService.deleteParentGroup(groupId, parentGroupId);
    }

    @RequestMapping(value = "/group/{groupId}/child/{childId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addChildGroup(@PathVariable("groupId") Long groupId, @PathVariable("childId") Long childGroupId) {
        groupService.addChildGroup(groupId, childGroupId);
    }

    @RequestMapping(value = "/group/{groupId}/child/{childId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteChildGroup(@PathVariable("groupId") Long groupId, @PathVariable("childId") Long childGroupId) {
        groupService.deleteChildGroup(groupId, childGroupId);
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
    public ResponseEntity<List<GroupStatisticTO>> getStatistics(@PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(groupStatisticService.getMonthlyGroupStatistics(groupId), HttpStatus.OK);
    }


    // migration only
    @RequestMapping(value = "/group/{groupId}/migratestatistics", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void migrateStatistics(@PathVariable("groupId") Long groupId, @RequestBody List<GroupStatistic> statistics)
            throws ResourceNotFoundException {
        groupStatisticService.migrateStatistics(groupId, statistics);
    }


    // Second stage of forgotten password, if username or email have been forgotten
    @RequestMapping(value = "/public/passwordrequest/group/{groupId}", method = RequestMethod.POST)
    @ResponseBody
    public void passwordRequest(@PathVariable("groupId") Long groupId, @RequestBody UnitRequest unitRequest)
            throws ResourceNotFoundException, MailException, MessagingException {
        groupService.passwordRequest(groupId, unitRequest);
    }

    @RequestMapping(value = "/user/{userId}/messaginggroups", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<BaseGroup>> getMessagingGroupsForUser(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(groupService.findMessagingGroupsByUserId(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/groups", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.Group>> getUserGroups(@PathVariable("userId") Long userId
            , GetParameters getParameters) {
        return new ResponseEntity<>(groupService.getUserGroups(userId, getParameters), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/groups/alldetails", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<Group>> getUserGroupsFull(@PathVariable("userId") Long userId
            , GetParameters getParameters) {
        return new ResponseEntity<>(groupService.getUserGroupsAllDetails(userId, getParameters), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/allowedrelationshipgroups", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.Group>> getAllowedRelationshipGroups(
            @PathVariable("userId") Long userId) {
        return new ResponseEntity<>(groupService.getAllowedRelationshipGroups(userId), HttpStatus.OK);
    }
}
