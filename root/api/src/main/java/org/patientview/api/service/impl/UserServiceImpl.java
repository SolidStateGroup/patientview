package org.patientview.api.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.patientview.api.controller.model.Email;
import org.patientview.api.exception.ResourceNotFoundException;
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
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
    private LookupRepository lookupRepository;

    @Inject
    private EntityManager entityManager;

    @Inject
    private Properties properties;

    private User createUser(User user) {

        User newUser;

        if (getByUsername(user.getUsername()) != null) {
            throw new EntityExistsException("User already exists (username)");
        }

        if (getByEmail(user.getEmail()) != null) {
            throw new EntityExistsException("User already exists (email)");
        }

        user.setCreator(userRepository.findOne(1L));
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

        entityManager.flush();

        if (!CollectionUtils.isEmpty(user.getUserFeatures())) {

            for (UserFeature userFeature : user.getUserFeatures()) {
                userFeature.setFeature(featureRepository.findOne(userFeature.getFeature().getId()));
                userFeature.setUser(userRepository.findOne(userId));
                userFeature.setCreator(userRepository.findOne(1L));
                userFeatureRepository.save(userFeature);
            }
        }

        entityManager.flush();

        // TODO remove into a separate call
        if (!CollectionUtils.isEmpty(user.getIdentifiers())) {

            for (Identifier identifier : user.getIdentifiers()) {
                identifier.setId(null);
                identifier.setUser(userRepository.findOne(userId));
                identifier.setCreator(userRepository.findOne(1L));
                identifierRepository.save(identifier);
            }
        }

        entityManager.flush();

        user.setId(newUser.getId());

        // Everyone should be in the generic group.
        addUserToGenericGroup(newUser);

        return userRepository.save(user);

    }

    // We do this so early one gets the generic group
    private void addUserToGenericGroup(User user) {
        // TODO Sprint 2 make these value configurable
        Role role = roleRepository.findOne(7L);
        Group group = groupRepository.findOne(1L);

        GroupRole groupRole = new GroupRole();
        groupRole.setUser(user);
        groupRole.setGroup(group);
        groupRole.setCreator(userRepository.findOne(1L));
        groupRole.setRole(role);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);
    }


    public User createUserWithPasswordEncryption(User user) {
        user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
        return createUser(user);
    }


    public User createUserResetPassword(User user) {
        user.setPassword(DigestUtils.sha256Hex(CommonUtils.getAuthtoken()));
        return createUser(user);
    }

    //Migration Only
    public User createUserNoEncryption(User user) {
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

        Set<GroupRole> groupRoles = user.getGroupRoles();
        Set<Identifier> identifiers = user.getIdentifiers();
        Set<UserFeature> features = user.getUserFeatures();

        user.setIdentifiers(Collections.EMPTY_SET);
        user.setGroupRoles(Collections.EMPTY_SET);
        user.setUserFeatures(Collections.EMPTY_SET);
        groupRoleRepository.deleteByUser(user);
        userFeatureRepository.deleteByUser(user);
        identifierRepository.deleteByUser(user);
        entityManager.flush();

        entityManager.merge(user);
        user = userRepository.save(user);

        User creator = userRepository.getOne(1L);

        for (GroupRole groupRole : groupRoles) {
            GroupRole newGroupRole = new GroupRole();
            newGroupRole.setGroup(groupRepository.findOne(groupRole.getGroup().getId()));
            newGroupRole.setRole(roleRepository.findOne((groupRole.getRole().getId())));
            newGroupRole.setUser(user);
            newGroupRole.setCreator(creator);
            entityManager.merge(newGroupRole);
            entityManager.persist(newGroupRole);
        }

        for (Identifier identifier : identifiers) {
            if (identifier.getId() != null && identifier.getId() < 0) {
                identifier.setId(null);
            }
            Identifier newIdentifier = new Identifier();
            newIdentifier.setCreator(creator);
            newIdentifier.setUser(userRepository.findOne(user.getId()));
            newIdentifier.setIdentifierType(lookupRepository.findOne(identifier.getIdentifierType().getId()));
            newIdentifier.setIdentifier(identifier.getIdentifier());
            entityManager.merge(newIdentifier);
            entityManager.persist(newIdentifier);
        }

        for (UserFeature userFeature : features) {
            UserFeature newUserFeature = new UserFeature();
            newUserFeature.setFeature(featureRepository.findOne(userFeature.getFeature().getId()));
            newUserFeature.setCreator(creator);
            newUserFeature.setUser(userRepository.findOne(user.getId()));
            entityManager.merge(newUserFeature);
            entityManager.persist(newUserFeature);
        }

        return user;
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

        StringBuilder sb = new StringBuilder();
        sb.append("Please visit http://www.patientview.org/#/verify?userId=");
        sb.append(user.getId());
        sb.append("&verificationCode=");
        sb.append(user.getVerificationCode());
        sb.append(" to validate your account.");
        email.setBody(sb.toString());
        return emailService.sendEmail(email);
    }

    public Boolean verify(Long userId, String verificationCode) throws ResourceNotFoundException {
        User user = userRepository.getOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user {}" + userId);
        }
        if (user.getVerificationCode().equals(verificationCode)) {
            user.setVerified(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Identifier createUserIdentifier(Long userId, Identifier identifier) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find user {}" + userId);
        }
        user.getIdentifiers().add(identifier);
        identifier.setUser(user);
        userRepository.save(user);

        return identifier;
    }

}
