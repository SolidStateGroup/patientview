package org.patientview.api.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.patientview.api.controller.model.Email;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.Properties;

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

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private EmailService emailService;

    @Inject
    private Properties properties;

    public User createUser(User user) {

        User newUser;

        /*try {
            newUser = userRepository.save(user);
        } catch (DataIntegrityViolationException dve) {
            LOG.debug("User not created, duplicate user: {}", dve.getCause());
            throw new EntityExistsException("Username already exists");
        } catch (ConstraintViolationException cve) {
            LOG.debug("User not created, duplicate user: {}", cve.getCause());
            throw new EntityExistsException("Username already exists");
        }*/

        if (getByUsername(user.getUsername()) != null) {
            throw new EntityExistsException("User already exists (username)");
        }

        if (getByEmail(user.getEmail()) != null) {
            throw new EntityExistsException("User already exists (email)");
        }

        user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
        newUser = userRepository.save(user);
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

            for (UserFeature userFeature : user.getUserFeatures()) {
                userFeature.setFeature(featureRepository.findOne(userFeature.getFeature().getId()));
                userFeature.setUser(userRepository.findOne(userId));
                userFeature.setCreator(userRepository.findOne(1L));
                userFeatureRepository.save(userFeature);
            }
        }

        if (!CollectionUtils.isEmpty(user.getIdentifiers())) {

            for (Identifier identifier : user.getIdentifiers()) {
                identifier.setId(null);
                identifier.setUser(userRepository.findOne(userId));
                identifier.setCreator(userRepository.findOne(1L));
                identifierRepository.save(identifier);
            }
        }

        user.setId(newUser.getId());

        return userRepository.save(user);
    }


    public User createUserResetPassword(User user) {
        user.setPassword(DigestUtils.sha256Hex(CommonUtils.getAuthtoken()));
        return createUser(user);
    }

    public User getUser(Long userId) {
        return userRepository.findOne(userId);

    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {

        // clear existing user groups roles, features
        User entityUser = userRepository.findOne(user.getId());
        groupRoleRepository.delete(entityUser.getGroupRoles());
        userFeatureRepository.delete(entityUser.getUserFeatures());
        identifierRepository.delete(entityUser.getIdentifiers());

        // add updated groups and roles
        if (!CollectionUtils.isEmpty(user.getGroupRoles())) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                groupRole.setGroup(groupRepository.findOne(groupRole.getGroup().getId()));
                groupRole.setRole(roleRepository.findOne(groupRole.getRole().getId()));
                groupRole.setUser(userRepository.findOne(user.getId()));
                groupRole.setCreator(userRepository.findOne(1L));
                groupRoleRepository.save(groupRole);
            }
        }

        // add updated features
        if (!CollectionUtils.isEmpty(user.getUserFeatures())) {
            for (UserFeature userFeature : user.getUserFeatures()) {
                userFeature.setFeature(featureRepository.findOne(userFeature.getFeature().getId()));
                userFeature.setUser(userRepository.findOne(user.getId()));
                userFeature.setCreator(userRepository.findOne(1L));
                userFeatureRepository.save(userFeature);
            }
        }

        // add identifiers
        if (!CollectionUtils.isEmpty(user.getIdentifiers())) {
            for (Identifier identifier : user.getIdentifiers()) {
                identifier.setId(null);
                identifier.setUser(userRepository.findOne(user.getId()));
                identifier.setCreator(userRepository.findOne(1L));
                identifierRepository.save(identifier);
            }
        }

        return userRepository.save(user);
    }

    public List<User> getUserByGroupAndRole(Long groupId, Long roleId) {
        Group group = groupRepository.findOne(groupId);
        Role role = roleRepository.findOne(roleId);

        return Util.iterableToList(userRepository.findByGroupAndRole(group, role));

    }

    public List<User> getUsersByGroupsAndRoles(List<Long> groupIds, List<Long> roleIds) {
        return Util.iterableToList(userRepository.findByGroupsAndRoles(groupIds, roleIds));
    }

    public void deleteUser(Long userId) {
        userRepository.delete(userId);
    }

    public List<Feature> getUserFeatures(Long userId) {
        User user = userRepository.getOne(userId);
        return Util.iterableToList(Util.iterableToList(featureRepository.findByUser(user)));
    }

    public User updatePassword(Long userId, String password) {
        User user = userRepository.getOne(userId);
        user.setPassword(DigestUtils.sha256Hex(password));
        return userRepository.save(user);
    }

    public Boolean sendVerificationEmail(Long userId) {
        User user = userRepository.getOne(userId);
        Email email = new Email();
        email.setSender(properties.getProperty("smtp.sender"));
        email.setSubject("PatientView - Please verify your account");
        email.setRecipients(new String[]{user.getEmail()});
        email.setBody("Please visit http://www.patientview.org/#/verify?userId="
                + user.getId()
                + "&verificationCode="
                + user.getVerificationCode()
                + " to validate your account.");
        return emailService.sendEmail(email);
    }

    public Boolean verify(Long userId, String verificationCode) {
        User user = userRepository.getOne(userId);
        if (user.getVerificationCode().equals(verificationCode)) {
            user.setVerified(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

}
