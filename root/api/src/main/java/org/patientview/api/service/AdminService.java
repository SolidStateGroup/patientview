package org.patientview.api.service;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This service is to administer the User/Groups and Roles
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AdminService {

    Group createGroup(Group group);

    User getUser(Long userId);

    User createUser(User user);

    User saveUser(User user);

    Group getGroup(Long groupId);

    GroupFeature addGroupFeature(Long groupId, Long featureId);

    List<Group> getAllGroups();

    List<Role> getAllRoles();

    User getByUsername(String username);

    GroupFeature createGroupFeature(GroupFeature groupFeature);

    List<Route> getUserRoutes(Long userId);

    List<User> getUserByGroupAndRole(Long groupId, Long roleId);

}
