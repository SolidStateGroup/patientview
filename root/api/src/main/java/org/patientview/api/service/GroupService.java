package org.patientview.api.service;

import org.patientview.api.annotation.AuditTrail;
import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.api.model.BaseGroup;
import org.patientview.api.model.UnitRequest;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.mail.MailException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupService {

    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Group get(Long id) throws ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    List<Group> findAll();

    List<org.patientview.api.model.Group> findAllPublic();

    Group findByCode(String code);

    List<Group> findGroupsByUser(User user);

    @UserOnly
    List<BaseGroup> findMessagingGroupsByUserId(Long userId) throws ResourceNotFoundException;

    @AuditTrail(value = AuditActions.GROUP_EDIT, objectType = Group.class)
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(Group group) throws ResourceNotFoundException, EntityExistsException, ResourceForbiddenException;

    @AuditTrail(value = AuditActions.GROUP_ADD, objectType = Group.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Long add(Group group);

    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addParentGroup(Long groupId, Long parentGroupId);

    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteParentGroup(Long groupId, Long parentGroupId);

    List<Group> findChildren(Long groupId) throws ResourceNotFoundException;

    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addChildGroup(Long groupId, Long childGroupId);

    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteChildGroup(Long groupId, Long childGroupId);

    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void addFeature(Long groupId, Long featureId);

    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteFeature(Long groupId, Long featureId);

    void passwordRequest(Long groupId, UnitRequest unitRequest)
            throws ResourceNotFoundException, MailException, MessagingException;

    List<Group> addParentAndChildGroups(List<Group> groups);

    List<UUID> getOrganizationLogicalUuidsByCode(final String code) throws FhirResourceException;

    UUID addOrganization(Group group) throws FhirResourceException;

    // previously security service

    /**
     * Get the groups that are assigned to the user.
     * N.B. SuperAdmin gets them all/
     *
     * @param userId
     * @return
     */
    @UserOnly
    Page<org.patientview.api.model.Group> getUserGroups(Long userId, GetParameters getParameters);

    @UserOnly
    List<Group> getAllUserGroupsAllDetails(Long userId);

    @UserOnly
    Page<Group> getUserGroupsAllDetails(Long userId, GetParameters getParameters);

    // allowed relationship groups are those that can be added as parents or children to existing groups
    // GLOBAL_ADMIN can see all groups so allowedRelationshipGroups is identical to those returned from getGroupsForUser
    // SPECIALTY_ADMIN can only edit their specialty and add relationships
    // all other users cannot add parents/children
    @UserOnly
    Page<org.patientview.api.model.Group> getAllowedRelationshipGroups(Long userId);
}
