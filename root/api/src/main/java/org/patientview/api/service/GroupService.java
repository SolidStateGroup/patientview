package org.patientview.api.service;

import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.Roles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupService {

    //@GroupMemberOnly(roles = {Roles.UNIT_ADMIN, Roles.STAFF_ADMIN})
    Group findOne(Long id);

    List<Group> findAll();

    List<Group> findGroupByUser(User user);

    List<Group> findGroupAndChildGroupsByUser(User user);

    List<Group> findGroupByType(Long lookupId);

    //@GroupMemberOnly(roles = {Roles.UNIT_ADMIN, Roles.STAFF_ADMIN})
    Group save(Group group);

    Group create(Group group);

    GroupRole addGroupRole(Long userId, Long groupId, Long roleId);

    void addParentGroup(Long groupId, Long parentGroupId);

    Link addLink(Long groupId, Link link);

    Location addLocation(Long groupId, Location location);

    void addFeature(Long groupId, Long featureId);

    void deleteFeature(Long groupId, Long featureId);

}
