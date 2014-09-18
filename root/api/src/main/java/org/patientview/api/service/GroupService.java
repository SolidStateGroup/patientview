package org.patientview.api.service;

import org.patientview.api.annotation.AuditTrail;
import org.patientview.api.model.UnitRequest;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupService {

    //@GroupMemberOnly(roles = {RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN})
    Group get(Long id);

    List<Group> findAll();

    List<Group> findGroupByUser(User user);

    List<Group> findGroupByType(Long lookupId);

    //@GroupMemberOnly(roles = {RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN})
    Group save(Group group) throws ResourceNotFoundException, EntityExistsException;

    @AuditTrail(value = AuditActions.CREATE, objectType = Group.class)
    Group add(Group group);

    GroupRole addGroupRole(Long userId, Long groupId, Long roleId);

    void deleteGroupRole(Long userId, Long groupId, Long roleId);

    void addParentGroup(Long groupId, Long parentGroupId);

    List<Group> findChildren(Long groupId) throws ResourceNotFoundException;

    void deleteParentGroup(Long groupId, Long parentGroupId);

    void addChildGroup(Long groupId, Long childGroupId);

    void deleteChildGroup(Long groupId, Long childGroupId);

    Link addLink(Long groupId, Link link);

    ContactPoint addContactPoint(Long groupId, ContactPoint contactPoint);

    Location addLocation(Long groupId, Location location);

    void addFeature(Long groupId, Long featureId);

    void deleteFeature(Long groupId, Long featureId);

    void contactUnit(Long groupId, UnitRequest unitRequest) throws ResourceNotFoundException, ResourceInvalidException;

    // now public
    public List<Group> addParentAndChildGroups(List<Group> groups);
}
