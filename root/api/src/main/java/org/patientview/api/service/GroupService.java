package org.patientview.api.service;

import org.patientview.api.annotation.AuditTrail;
import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.BaseGroup;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.UUID;

/**
 * Group service, for managing and retrieving Group information.
 *
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupService {

    @RoleOnly(roles = { RoleName.GLOBAL_ADMIN })
    String evictAllCaches();

    /**
     * Create a Group.
     * @param group Group object containing all required properties
     * @return Long ID of Group created successfully
     */
    @AuditTrail(value = AuditActions.GROUP_ADD, objectType = Group.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Long add(Group group);

    /**
     * Add a Group as a child Group to another Group, defining a parent -> child relationship, e.g. Specialty -> Unit.
     * @param groupId ID of parent Group to add child Group to
     * @param childGroupId ID of child Group to be added
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addChildGroup(Long groupId, Long childGroupId);

    /**
     * Add a Feature to a Group.
     * @param groupId ID of Group to add Feature to
     * @param featureId ID of Feature to add
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addFeature(Long groupId, Long featureId);

    /**
     * Add a FHIR Organization, used when adding transplant status from UKT, also used during migration.
     * @param group Group to use to create FHIR Organization
     * @return UUID of newly created FHIR Organization
     * @throws FhirResourceException
     */
    UUID addOrganization(Group group) throws FhirResourceException;

    /**
     * Add a Group as a parent Group of another Group, defining a parent -> child relationship, e.g. Specialty -> Unit.
     * Note: consider consolidating with addChildGroup() method.
     * @param groupId ID of child Group to be added
     * @param parentGroupId ID of parent Group to add child Group to
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addParentGroup(Long groupId, Long parentGroupId);

    /**
     * Remove a child Group from a parent Group.
     * @param groupId ID of parent Group to remove child Group from
     * @param childGroupId ID of child Group to remove from parent Group
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteChildGroup(Long groupId, Long childGroupId);

    /**
     * Remove a Feature from a Group.
     * @param groupId ID of Group to remove Feature from
     * @param featureId ID of Feature to remove
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteFeature(Long groupId, Long featureId);

    /**
     * Remove a parent Group from a child Group. Note: consider consolidating with deleteChildGroup() method.
     * @param groupId ID of child Group to remove from parent Group
     * @param parentGroupId ID of parent Group to remove child Group from
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteParentGroup(Long groupId, Long parentGroupId);

    /**
     * Get all Groups.
     * @deprecated
     * @return List of all Groups
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    List<Group> findAll();

    /**
     * Get publicly available information about all Groups.
     * @return List of publicly available Group objects
     */
    List<org.patientview.api.model.Group> findAllPublic();

    /**
     * Find a single Group by its unique Group code.
     * @param code String code of a Group
     * @return Group object
     */
    Group findByCode(String code);

    /**
     * Get children of a Group given ID.
     * @param groupId ID of Group to get child Groups for
     * @return List of child Groups
     * @throws ResourceNotFoundException
     */
    List<Group> findChildren(Long groupId) throws ResourceNotFoundException;

    /**
     * Find all Groups that a User belongs to.
     * @param user User to find Groups for
     * @return List of Groups that a User belongs to
     */
    List<Group> findGroupsByUser(User user);

    /**
     * Find all Groups with messaging enabled that a User can contact.
     * @param userId ID of User to find Groups for
     * @return List of BaseGroup
     * @throws ResourceNotFoundException
     */
    @UserOnly
    List<BaseGroup> findMessagingGroupsByUserId(Long userId) throws ResourceNotFoundException;

    /**
     * Get a single Group given ID.
     * @param id ID of Group to find
     * @return Group object
     * @throws ResourceForbiddenException
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.GP_ADMIN })
    Group get(Long id) throws ResourceForbiddenException;

    /**
     * Get a Page of Groups that are allowed relationship Groups given a User ID and their permissions. Allowed
     * relationship Groups are those that can be added as parents or children to existing groups by that User. Some
     * Users may be able to add children to any Group but others are restricted. Note: consider refactor.
     * Note: GLOBAL_ADMIN can see all groups so allowedRelationshipGroups is identical to those returned from
     * getGroupsForUser, SPECIALTY_ADMIN can only edit their specialty and add relationships, all other users cannot add
     * parents/children.
     * @param userId ID of User to get allowed relationship Groups
     * @return Page of allowed relationship Groups
     */
    @UserOnly
    Page<org.patientview.api.model.Group> getAllowedRelationshipGroups(Long userId);

    /**
     * Find all Groups that a User belongs to, including all child properties, used during authentication.
     * @param userId ID of User to find Groups for
     * @return List of Groups
     */
    List<Group> getAllUserGroupsAllDetails(Long userId);

    /**
     * Get List of Groups by feature name, currently used to get list of groups with MESSAGING feature for creating
     * membership request Conversations.
     * @param featureName String name of feature that Group must have
     * @return List of Groups
     */
    List<org.patientview.api.model.Group> getByFeature(String featureName)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a List of FHIR Organization UUIDs from FHIR given an FHIR Organization/Group code.
     * @param code String code of a Group/Organization
     * @return List of FHIR Organization UUIDs
     * @throws FhirResourceException
     */
    List<UUID> getOrganizationLogicalUuidsByCode(final String code) throws FhirResourceException;

    /**
     * Get a Page of Groups that a User can access, given GetParameters for filters, page size, number etc.
     * @param userId ID of User retrieving Groups
     * @param getParameters GetParameters object containing filters, page size, number etc
     * @return Page of Group objects
     */
    @UserOnly
    Page<org.patientview.api.model.Group> getUserGroups(Long userId, GetParameters getParameters);

    /**
     * Get a Page of Groups that a User can access, given GetParameters for filters, page size, number etc. This
     * includes all information on each Group so may return a large JSON object. Used on Contact Your Unit page.
     * @param userId ID of User retrieving Groups
     * @param getParameters GetParameters object containing filters, page size, number etc
     * @return Page of Group objects
     */
    @UserOnly
    Page<Group> getUserGroupsAllDetails(Long userId, GetParameters getParameters);

    /**
     * Check if Group ID is member of CENTRAL_SUPPORT groups.
     * @param groupId ID of Group to check is a CENTRAL_SUPPORT Group
     * @return True if Group is CENTRAL_SUPPORT type
     */
    boolean groupIdIsSupportGroup(Long groupId) throws ResourceNotFoundException;

    /**
     * Save an updated Group.
     * @param group Group to save
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @AuditTrail(value = AuditActions.GROUP_EDIT, objectType = Group.class)
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(Group group) throws ResourceNotFoundException, EntityExistsException, ResourceForbiddenException;
}
