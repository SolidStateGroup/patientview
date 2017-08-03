package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.BaseGroup;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * RESTful interface for managing Groups and retrieving Group related information.
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@RestController
@ExcludeFromApiDoc
public class GroupController extends BaseController<GroupController> {

    @Inject
    private GroupService groupService;

    @Inject
    private GroupStatisticService groupStatisticService;

    /**
     * Create a Group.
     * @param group Group object containing all required properties
     * @return Long ID of Group created successfully
     */
    @RequestMapping(value = "/group", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> add(@RequestBody Group group) {
        return new ResponseEntity<>(groupService.add(group), HttpStatus.OK);
    }

    /**
     * Add a Group as a child Group to another Group, defining a parent -> child relationship, e.g. Specialty -> Unit.
     * @param groupId ID of parent Group to add child Group to
     * @param childGroupId ID of child Group to be added
     */
    @RequestMapping(value = "/group/{groupId}/child/{childId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addChildGroup(@PathVariable("groupId") Long groupId, @PathVariable("childId") Long childGroupId) {
        groupService.addChildGroup(groupId, childGroupId);
    }

    /**
     * Add a Feature to a Group.
     * @param groupId ID of Group to add Feature to
     * @param featureId ID of Feature to add
     */
    @RequestMapping(value = "/group/{groupId}/features/{featureId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addFeature(@PathVariable("groupId") Long groupId, @PathVariable("featureId") Long featureId) {
        groupService.addFeature(groupId, featureId);
    }

    /**
     * Add a Group as a parent Group of another Group, defining a parent -> child relationship, e.g. Specialty -> Unit.
     * Note: consider consolidating with addChildGroup() method.
     * @param groupId ID of child Group to be added
     * @param parentGroupId ID of parent Group to add child Group to
     */
    @RequestMapping(value = "/group/{groupId}/parent/{parentId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addParentGroup(@PathVariable("groupId") Long groupId,
                               @PathVariable("parentId") Long parentGroupId) {
        groupService.addParentGroup(groupId, parentGroupId);
    }

    /**
     * Remove a child Group from a parent Group.
     * @param groupId ID of parent Group to remove child Group from
     * @param childGroupId ID of child Group to remove from parent Group
     */
    @RequestMapping(value = "/group/{groupId}/child/{childId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteChildGroup(@PathVariable("groupId") Long groupId, @PathVariable("childId") Long childGroupId) {
        groupService.deleteChildGroup(groupId, childGroupId);
    }

    /**
     * Remove a Feature from a Group.
     * @param groupId ID of Group to remove Feature from
     * @param featureId ID of Feature to remove
     */
    @RequestMapping(value = "/group/{groupId}/features/{featureId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteFeature(@PathVariable("groupId") Long groupId, @PathVariable("featureId") Long featureId) {
        groupService.deleteFeature(groupId, featureId);
    }

    /**
     * Remove a parent Group from a child Group. Note: consider consolidating with deleteChildGroup() method.
     * @param groupId ID of child Group to remove from parent Group
     * @param parentGroupId ID of parent Group to remove child Group from
     */
    @RequestMapping(value = "/group/{groupId}/parent/{parentId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteParentGroup(@PathVariable("groupId") Long groupId, @PathVariable("parentId") Long parentGroupId) {
        groupService.deleteParentGroup(groupId, parentGroupId);
    }

    /**
     * Get a Page of Groups that are allowed relationship Groups given a User ID and their permissions. Allowed
     * relationship Groups are those that can be added as parents or children to existing groups by that User. Some
     * Users may be able to add children to any Group but others are restricted. Note: consider refactor.
     * @param userId ID of User to get allowed relationship Groups
     * @return Page of allowed relationship Groups
     */
    @RequestMapping(value = "/user/{userId}/allowedrelationshipgroups", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.Group>> getAllowedRelationshipGroups(
            @PathVariable("userId") Long userId) {
        return new ResponseEntity<>(groupService.getAllowedRelationshipGroups(userId), HttpStatus.OK);
    }

    /**
     * Get List of Groups by feature name, currently used to get list of groups with MESSAGING feature for creating
     * membership request Conversations.
     * @param featureName String name of feature that Group must have
     * @return List of Groups
     */
    @RequestMapping(value = "/group/feature/{featureName}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<org.patientview.api.model.Group>> getByFeature(
            @PathVariable(value = "featureName") String featureName)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(groupService.getByFeature(featureName), HttpStatus.OK);
    }

    /**
     * Get a single Group.
     * @param groupId ID of Group to get
     * @return Group object
     * @throws SecurityException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/group/{groupId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Group> getGroup(@PathVariable("groupId") Long groupId)
            throws SecurityException, ResourceForbiddenException {
        return new ResponseEntity<>(groupService.get(groupId), HttpStatus.OK);
    }

    /**
     * Get all Groups.
     * @deprecated
     * @return List of all Groups
     */
    @RequestMapping(value = "/group", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<Group>> getGroups() {
        return new ResponseEntity<>(groupService.findAll(), HttpStatus.OK);
    }

    /**
     * Get publicly available information about all Groups.
     * @return List of publicly available Group objects
     */
    @RequestMapping(value = "/public/group", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<org.patientview.api.model.Group>> getGroupsPublic() {
        return new ResponseEntity<>(groupService.findAllPublic(), HttpStatus.OK);
    }

    /**
     * Get list of Groups with MESSAGING Feature and Users that can be contacted by a User. On creating Conversation,
     * where a User must select from a list of available Groups and then select a recipient.
     * @param userId ID of User to retrieve
     * @return List of BaseGroup containing minimal information on the Groups that can be contacted
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/messaginggroups", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<BaseGroup>> getMessagingGroupsForUser(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(groupService.findMessagingGroupsByUserId(userId), HttpStatus.OK);
    }

    /**
     * Get a Page of Groups that a User can access, given GetParameters for filters, page size, number etc.
     * @param userId ID of User retrieving Groups
     * @param getParameters GetParameters object containing filters, page size, number etc
     * @return Page of Group objects
     */
    @RequestMapping(value = "/user/{userId}/groups", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.Group>> getUserGroups(@PathVariable("userId") Long userId
            , GetParameters getParameters) {
        return new ResponseEntity<>(groupService.getUserGroups(userId, getParameters), HttpStatus.OK);
    }

    /**
     * Get a Page of Groups that a User can access, given GetParameters for filters, page size, number etc. This
     * includes all information on each Group so may return a large JSON object. Used on Contact Your Unit page.
     * @param userId ID of User retrieving Groups
     * @param getParameters GetParameters object containing filters, page size, number etc
     * @return Page of Group objects
     */
    @RequestMapping(value = "/user/{userId}/groups/alldetails", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<Group>> getUserGroupsFull(@PathVariable("userId") Long userId
            , GetParameters getParameters) {
        return new ResponseEntity<>(groupService.getUserGroupsAllDetails(userId, getParameters), HttpStatus.OK);
    }

    // migration only
    @RequestMapping(value = "/group/{groupId}/migratestatistics", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void migrateStatistics(@PathVariable("groupId") Long groupId, @RequestBody List<GroupStatistic> statistics)
            throws ResourceNotFoundException {
        groupStatisticService.migrateStatistics(groupId, statistics);
    }

    /**
     * Save an updated Group.
     * @param group Group to save
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/group", method = RequestMethod.PUT)
    @ResponseBody
    public void saveGroup(@RequestBody Group group) throws ResourceNotFoundException, ResourceForbiddenException {
        groupService.save(group);
    }
}
