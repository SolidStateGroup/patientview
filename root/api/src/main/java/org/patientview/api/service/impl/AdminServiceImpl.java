package org.patientview.api.service.impl;

import org.patientview.api.service.AdminService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private GroupFeatureRepository groupFeatureRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private RouteRepository routeRepository;

    public User getUser(Long userId) {
        return userRepository.findOne(userId);

    }

    public Group createGroup(Group group) {
        if (group.getGroupType() != null) {
            group.setGroupType(lookupRepository.findOne(group.getGroupType().getId()));
        }

        if (!CollectionUtils.isEmpty(group.getGroupFeatures())) {
            for (GroupFeature groupFeature : group.getGroupFeatures()) {
                groupFeature = groupFeatureRepository.findOne(groupFeature.getId());
            }
        }

        return groupRepository.save(group);
    }

    /**
     * TODO Sort of the cascade model for GroupRoles
     *
     * @param user
     * @return
     */
    public User createUser(User user) {


        User newUser = userRepository.save(user);
        Long userId = newUser.getId();
        LOG.info("New user with id: {}", user.getId());

        if (!CollectionUtils.isEmpty(user.getGroupRoles())) {

            for (GroupRole groupRole : user.getGroupRoles()) {

                groupRole.setGroup(groupRepository.findOne(groupRole.getGroup().getId()));
                groupRole.setRole(roleRepository.findOne(groupRole.getRole().getId()));
                groupRole.setUser(userRepository.findOne(userId));
                groupRole.setCreator(userRepository.findOne(1L));
                groupRoleRepository.save(groupRole);
            }

        }

        user.setId(newUser.getId());

        return userRepository.save(user);
    }

    public Group getGroup(Long groupId) { return groupRepository.findOne(groupId);}

    public GroupFeature addGroupFeature(Long groupId, Long featureId) {

        GroupFeature groupFeature = new GroupFeature();
        groupFeature.setStartDate(new Date());
        groupFeature.setCreator(userRepository.findOne(1L));
        groupFeature.setFeature(featureRepository.findOne(featureId));
        groupFeature.setGroup(groupRepository.findOne(groupId));
        return groupFeatureRepository.save(groupFeature);
    }

    public List<Group> getAllGroups() {
        return Util.iterableToList(groupRepository.findAll());
    }

    public List<Role> getAllRoles() {
        return Util.iterableToList(roleRepository.findAll());
    }

    //TODO
    public List<Feature> getUserFeatures(User user) {

        return null;
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        User entityUser = userRepository.findOne(user.getId());
        entityUser.setFhirResourceId(user.getFhirResourceId());
        return userRepository.save(user);
    }

    public GroupFeature createGroupFeature(GroupFeature groupFeature) {
        return groupFeatureRepository.save(groupFeature);
    }

    //TODO - Currently returns all routes
    public List<Route> getUserRoutes(Long userId) {

        return Util.iterableToList(routeRepository.findAll());

    }


}
