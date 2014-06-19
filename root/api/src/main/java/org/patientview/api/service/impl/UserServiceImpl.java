package org.patientview.api.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserFeatureRepository userFeatureRepository;

    /**
     * This persists the User map with GroupRoles and UserFeatures. The static
     * data objects are detached so have to be become managed again without updating the objects.
     *
     * @param user
     * @return
     */
    public User createUser(User user) {

        user.setPassword(DigestUtils.sha256Hex(CommonUtils.getAuthtoken()));
        User newUser;
        try {
            newUser = userRepository.save(user);
        } catch (DataIntegrityViolationException dve) {
            LOG.debug("User not created, duplicate user: {}", dve.getCause());
            throw new EntityExistsException("Username already exists");
        }
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

        if (!CollectionUtils.isEmpty(user.getUserFeatures())) {

            for(UserFeature userFeature : user.getUserFeatures()) {
                userFeature.setFeature(featureRepository.findOne(userFeature.getFeature().getId()));
                userFeature.setUser(userRepository.findOne(userId));
                userFeature.setCreator(userRepository.findOne(1L));
                userFeatureRepository.save(userFeature);
            }

        }

        user.setId(newUser.getId());

        return userRepository.save(user);
    }

    public User getUser(Long userId) {
        return userRepository.findOne(userId);

    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User saveUser(User user) {
        User entityUser = userRepository.findOne(user.getId());
        entityUser.setFhirResourceId(user.getFhirResourceId());
        return userRepository.save(user);
    }


    public List<User> getUserByGroupAndRole(Long groupId, Long roleId) {
        Group group = groupRepository.findOne(groupId);
        Role role = roleRepository.findOne(roleId);

        return Util.iterableToList(userRepository.findByGroupAndRole(group, role));

    }

    public void deleteUser(Long userId) {
        userRepository.delete(userId);
    }

    // TODO put this into a JPASQL statement
    public List<Feature> getUserFeatures(Long userId) {

        List<Feature> features = new ArrayList<Feature>();
        User user = userRepository.findOne(userId);
        for (UserFeature userFeature : user.getUserFeatures()) {
            features.add(userFeature.getFeature());
        }

        for (GroupRole groupRole : user.getGroupRoles()) {
            for (GroupFeature groupFeature : groupRole.getGroup().getGroupFeatures()) {
                features.add(groupFeature.getFeature());
            }
        }

        return features;
        // return Util.iterableToList(Util.iterableToList(featureRepository.getFeaturesByUser(userId)));
    }

}
