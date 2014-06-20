package org.patientview.api.service.impl;

import org.patientview.api.service.AdminService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 *
 * TODO name change and refactor
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


    public Group createGroup(Group group) throws EntityExistsException {
        if (group.getGroupType() != null) {
            group.setGroupType(lookupRepository.findOne(group.getGroupType().getId()));
        }

        if (!CollectionUtils.isEmpty(group.getGroupFeatures())) {
            for (GroupFeature groupFeature : group.getGroupFeatures()) {
                groupFeature = groupFeatureRepository.findOne(groupFeature.getId());
            }
        }

        try {
            group = groupRepository.save(group);
        } catch (DataIntegrityViolationException dve) {
            LOG.debug("Group not created, duplicate: {}", dve.getCause());
            throw new EntityExistsException("Group already exists");
        }

        return group;

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

    public List<User> getGroupUserByRoleStaff(Long groupId) {
        return Util.iterableToList(groupRepository.getGroupStaffByRole(groupId, "STAFF"));
    }

    public List<User> getGroupUserByRolePatient(Long groupId) {
        return Util.iterableToList(groupRepository.getGroupStaffByRole(groupId, "PATIENT"));
    }

}
