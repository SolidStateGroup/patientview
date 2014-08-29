package org.patientview.api.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.patientview.api.controller.model.Email;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Service
public class UserServiceImpl extends AbstractServiceImpl<UserServiceImpl> implements UserService {

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
    private EntityManager entityManager;

    @Inject
    private Properties properties;

    private void addParentGroupRoles(GroupRole groupRole) {

        User entityUser = userRepository.findOne(groupRole.getUser().getId());
        Group entityGroup = groupRepository.findOne(groupRole.getGroup().getId());
        Role entityRole = roleRepository.findOne(groupRole.getRole().getId());

        // save grouprole with same role and parent group if doesn't exist already
        if (!CollectionUtils.isEmpty(entityGroup.getGroupRelationships())) {
            for (GroupRelationship groupRelationship : entityGroup.getGroupRelationships()) {
                if (groupRelationship.getRelationshipType() == RelationshipTypes.PARENT) {
                    Group parentEntityGroup =
                            groupRepository.findOne(groupRelationship.getObjectGroup().getId());
                    if (groupRoleRepository.findByUserGroupRole(entityUser, parentEntityGroup, entityRole)
                            == null) {
                        GroupRole parentGroupRole = new GroupRole();

                        parentGroupRole.setGroup(parentEntityGroup);
                        parentGroupRole.setRole(entityRole);
                        parentGroupRole.setUser(entityUser);
                        parentGroupRole.setCreator(userRepository.findOne(1L));
                        groupRoleRepository.save(parentGroupRole);
                    }
                }
            }
        }
    }

    public User add(User user) {

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

                Group entityGroup = groupRepository.findOne(groupRole.getGroup().getId());
                Role entityRole = roleRepository.findOne(groupRole.getRole().getId());

                // only save if doesn't already exist
                if (groupRoleRepository.findByUserGroupRole(newUser, entityGroup, entityRole) == null) {
                    groupRole.setGroup(entityGroup);
                    groupRole.setRole(entityRole);
                    groupRole.setUser(newUser);
                    groupRole.setCreator(userRepository.findOne(1L));
                    groupRoleRepository.save(groupRole);
                }

                addParentGroupRoles(groupRole);
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
        // Everyone should change their password at login
        user.setChangePassword(Boolean.TRUE);
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

    /**
     * This persists the User map with GroupRoles and UserFeatures. The static
     * data objects are detached so have to be become managed again without updating the objects.
     *
     * @param user
     * @return
     */
    public User createUserWithPasswordEncryption(User user) {
        user.setPassword(DigestUtils.sha256Hex(user.getPassword()));
        return add(user);
    }

    //Migration Only
    public User createUserNoEncryption(User user) {
        return add(user);
    }

    public User get(Long userId) {
        return userRepository.findOne(userId);
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) throws ResourceNotFoundException {
        User entityUser = findUser(user.getId());
        entityUser.setForename(user.getForename());
        entityUser.setSurname(user.getSurname());
        entityUser.setUsername(user.getUsername());
        entityUser.setEmail(user.getEmail());
        entityUser.setEmailVerified(user.getEmailVerified());
        entityUser.setLocked(user.getLocked());
        entityUser.setDummy(user.getDummy());
        entityUser.setContactNumber(user.getContactNumber());
        return userRepository.save(entityUser);
    }

    // todo: move to static class
    private List<Long> convertStringArrayToLongs(String[] strings) {
        final List<Long> longs = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(strings)) {
            for (String string : strings) {
                longs.add(Long.parseLong(string));
            }
        }
        return longs;
    }

    private List<org.patientview.api.model.User> convertUsersToTransportUsers(List<User> users) {
        List<org.patientview.api.model.User> transportUsers = new ArrayList<>();

        for (User user : users) {
            transportUsers.add(new org.patientview.api.model.User(user));
        }

        return transportUsers;
    }

    /**
     * Get users based on a list of groups and role types
     * @return
     */
    public Page<org.patientview.api.model.User> getUsersByGroupsAndRoles(GetParameters getParameters) {

        List<Long> groupIds = convertStringArrayToLongs(getParameters.getGroupIds());
        List<Long> roleIds = convertStringArrayToLongs(getParameters.getRoleIds());
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();
        String filterText = getParameters.getFilterText();

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortDirection)) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortDirection.equals("DESC")) {
                direction = Sort.Direction.DESC;
            }

            pageable = new PageRequest(pageConverted, sizeConverted, new Sort(new Sort.Order(direction, sortField)));
        } else {
            pageable = new PageRequest(pageConverted, sizeConverted);
        }

        if (StringUtils.isEmpty(filterText)) {
            filterText = "%%";
        } else {
            filterText = "%" + filterText.toUpperCase() + "%";
        }

        Page<User> users = userRepository.findByGroupsRoles(filterText, groupIds, roleIds, pageable);

        // convert to lightweight transport objects, create Page and return
        List<org.patientview.api.model.User> transportContent = convertUsersToTransportUsers(users.getContent());
        return new PageImpl<>(transportContent, pageable, users.getTotalElements());
    }

    public void delete(Long userId) {
        userRepository.delete(userId);
    }

    public List<Feature> getUserFeatures(Long userId) throws ResourceNotFoundException {
        User user = findUser(userId);
        return Util.convertIterable(Util.convertIterable(featureRepository.findByUser(user)));
    }

    /**
     * Reset the flag so the user will not be prompted to change the password again
     *
     * @param userId
     * @param password
     * @return
     */
    public User changePassword(Long userId, String password) throws ResourceNotFoundException {
        User user = findUser(userId);
        user.setChangePassword(Boolean.FALSE);
        user.setPassword(DigestUtils.sha256Hex(password));
        return userRepository.save(user);
    }

    /**
     * On a password reset the user should change on login
     *
     * @param userId
     * @param password
     * @return
     */
    public User resetPassword(Long userId, String password) throws ResourceNotFoundException {
        User user = findUser(userId);
        user.setChangePassword(Boolean.TRUE);
        user.setPassword(DigestUtils.sha256Hex(password));
        return userRepository.save(user);
    }

    /**
     * Send a email to the user email address to verify have access to the email account
     *
     * @param userId
     * @return
     */
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
        User user = findUser(userId);
        if (user.getVerificationCode().equals(verificationCode)) {
            user.setEmailVerified(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public Identifier addIdentifier(Long userId, Identifier identifier) throws ResourceNotFoundException {
        User user = findUser(userId);
        identifier.setCreator(userRepository.findOne(1L));
        user.getIdentifiers().add(identifier);
        identifier.setUser(user);

        return identifierRepository.save(identifier);
    }

    public void addFeature(Long userId, Long featureId) {
        UserFeature userFeature = new UserFeature();
        userFeature.setFeature(featureRepository.findOne(featureId));
        userFeature.setUser(userRepository.findOne(userId));
        userFeature.setCreator(userRepository.findOne(1L));
        userFeatureRepository.save(userFeature);
    }

    public void deleteFeature(Long userId, Long featureId) {
        userFeatureRepository.delete(userFeatureRepository.findByUserAndFeature(
                userRepository.findOne(userId), featureRepository.findOne(featureId)));
    }

    // Forgotten Password
    public void resetPasswordByUsernameAndEmail(String username, String email) throws ResourceNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find account");
        }

        if (user.getEmail().equalsIgnoreCase(email)) {
            user.setChangePassword(Boolean.TRUE);

            // Set the new password
            user.setPassword(CommonUtils.getAuthtoken());
            emailService.sendEmail(getForgottenPassword(user));
            // Hash the password
            user.setPassword(DigestUtils.sha256Hex(user.getPassword()));

            userRepository.save(user);
        } else {
            throw new ResourceNotFoundException("Could not find account");
        }

    }

    private User findUser(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException(String.format("Could not find user %s", userId));
        }
        return user;
    }

    private Email getForgottenPassword(User user) {
        Email email = new Email();
        email.setRecipients(new String[]{user.getEmail()});

        StringBuilder body = new StringBuilder();
        body.append("Your password has been reset\n");
        body.append("Your new password is :").append(user.getPassword()).append("\n");
        email.setBody(body.toString());
        email.setSubject("PatientView - Password Reset");
        return email;
    }

}
