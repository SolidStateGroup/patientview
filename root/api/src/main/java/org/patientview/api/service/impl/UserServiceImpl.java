package org.patientview.api.service.impl;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.patientview.api.model.BaseGroup;
import org.patientview.api.service.AuditService;
import org.patientview.api.service.ConversationService;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.ExternalServiceService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.PatientService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.ExternalServices;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GpMedicationGroupCodes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.StatusFilter;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserInformationRepository;
import org.patientview.persistence.repository.UserMigrationRepository;
import org.patientview.persistence.repository.UserObservationHeadingRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Service
@Transactional
public class UserServiceImpl extends AbstractServiceImpl<UserServiceImpl> implements UserService {

    @Inject
    private AlertRepository alertRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private ConversationService conversationService;

    @Inject
    private EmailService emailService;

    @Inject
    private EntityManager entityManager;

    @Inject
    private ExternalServiceService externalServiceService;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private PatientService patientService;

    @Inject
    private Properties properties;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserFeatureRepository userFeatureRepository;

    @Inject
    private UserInformationRepository userInformationRepository;

    @Inject
    private UserMigrationRepository userMigrationRepository;

    @Inject
    private UserObservationHeadingRepository userObservationHeadingRepository;

    @Inject
    private UserTokenRepository userTokenRepository;

    @Inject
    private UserRepository userRepository;

    // TODO make these value configurable
    private static final Long GENERIC_ROLE_ID = 7L;
    private static final Long GENERIC_GROUP_ID = 1L;
    // used in stats, set in SQL pv_lookup_value.description
    private static final int INACTIVE_MONTH_LIMIT = 3;
    // used for image resizing
    private static final int MAXIMUM_IMAGE_WIDTH = 400;
    private static final int THREE = 3;
    private static final int SIX = 6;
    private static final int EIGHT = 8;
    private static final int ONE_HUNDRED_AND_EIGHTY = 180;
    private static final int TWO_HUNDRED_AND_SEVENTY = 270;
    private static final int NINETY = 90;
    private Group genericGroup;
    private Role memberRole;

    @PostConstruct
    public void init() {
        // set up generic groups
        memberRole = roleRepository.findOne(GENERIC_ROLE_ID);
        genericGroup = groupRepository.findOne(GENERIC_GROUP_ID);
    }

    private void addParentGroupRoles(Long userId, GroupRole groupRole, User creator, boolean isPatient) {

        Group entityGroup = groupRepository.findOne(groupRole.getGroup().getId());

        // save grouprole with same role and parent group if doesn't exist already
        if (!CollectionUtils.isEmpty(entityGroup.getGroupRelationships())) {
            for (GroupRelationship groupRelationship : entityGroup.getGroupRelationships()) {
                if (groupRelationship.getRelationshipType() == RelationshipTypes.PARENT) {

                    if (!groupRoleRepository.userGroupRoleExists(groupRole.getUser().getId(),
                            groupRelationship.getObjectGroup().getId(), groupRole.getRole().getId()))
                    {
                        GroupRole parentGroupRole = new GroupRole();
                        parentGroupRole.setGroup(groupRelationship.getObjectGroup());
                        parentGroupRole.setRole(groupRole.getRole());
                        parentGroupRole.setUser(groupRole.getUser());
                        parentGroupRole.setCreator(creator);
                        groupRoleRepository.save(parentGroupRole);

                        if (isPatient) {
                            auditService.createAudit(AuditActions.PATIENT_GROUP_ROLE_ADD,
                                    groupRole.getUser().getUsername(),
                                    getCurrentUser(), userId, AuditObjectTypes.User, parentGroupRole.getGroup());
                        } else {
                            auditService.createAudit(AuditActions.ADMIN_GROUP_ROLE_ADD,
                                    groupRole.getUser().getUsername(),
                                    getCurrentUser(), userId, AuditObjectTypes.User, parentGroupRole.getGroup());
                        }
                    }
                }
            }
        }
    }

    public GroupRole addGroupRole(Long userId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {

        User creator = getCurrentUser();
        User user = findUser(userId);
        Group group = groupRepository.findOne(groupId);
        Role role = roleRepository.findOne(roleId);

        if (group == null || role == null) {
            throw new ResourceNotFoundException("Group or Role not found");
        }

        // validate i can add to requested group (staff role)
        if (!isUserMemberOfGroup(getCurrentUser(), group)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        if (groupRoleRepository.findByUserGroupRole(user, group, role) != null) {
            throw new EntityExistsException();
        }

        GroupRole groupRole = new GroupRole();
        groupRole.setUser(user);
        groupRole.setGroup(group);
        groupRole.setRole(role);
        groupRole.setCreator(creator);
        groupRole = groupRoleRepository.save(groupRole);

        boolean isPatient = role.getRoleType().getValue().equals(RoleType.PATIENT);

        if (isPatient) {
            auditService.createAudit(AuditActions.PATIENT_GROUP_ROLE_ADD, user.getUsername(),
                    getCurrentUser(), userId, AuditObjectTypes.User, group);

            // send membership notification to RDC, not GroupTypes.SPECIALTY
            if (!groupRole.getGroup().getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                sendGroupMemberShipNotification(groupRole, true);
            }
        } else {
            auditService.createAudit(AuditActions.ADMIN_GROUP_ROLE_ADD, user.getUsername(),
                    getCurrentUser(), userId, AuditObjectTypes.User, group);
        }

        addParentGroupRoles(userId, groupRole, creator, isPatient);
        return groupRole;
    }

    public void deleteGroupRole(Long userId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        deleteGroupRoleRelationship(userId, groupId, roleId, true);

        // if a user is removed from all child groups the parent group (if present) is also removed
        // e.g. remove Renal (specialty) if RenalA (unit) is removed and these are the only 2 groups present
        User user = findUser(userId);
        Group removedGroup = groupRepository.findOne(groupId);

        Set<GroupRole> toRemove = new HashSet<>();
        Set<GroupRole> userGroupRoles = new HashSet<>();

        // remove deleted grouprole from user.getGroupRoles as not deleted in this transaction yet
        for (GroupRole groupRole : user.getGroupRoles()) {
            if (!(groupRole.getGroup().getId().equals(groupId) && groupRole.getRole().getId().equals(roleId))) {
                userGroupRoles.add(groupRole);
            }
        }

        // identify specialty groups with no children
        for (GroupRole groupRole : userGroupRoles) {
            if (groupRole.getGroup().getGroupType() != null
                    && groupRole.getGroup().getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {

                List<Group> children = groupService.findChildren(groupRole.getGroup().getId());
                boolean childInGroupRoles = false;
                boolean removedGroupInChildren = children.contains(removedGroup);

                for (Group group : children) {
                    if (groupRolesContainsGroup(userGroupRoles, group)) {
                        childInGroupRoles = true;
                    }
                }

                if (!childInGroupRoles && removedGroupInChildren) {
                    toRemove.add(groupRole);
                }
            }
        }

        // remove any specialty groups with no children
        for (GroupRole groupRole : toRemove) {
            deleteGroupRoleRelationship(groupRole.getUser().getId(), groupRole.getGroup().getId(),
                    groupRole.getRole().getId(), false);
        }
    }

    private void sendGroupMemberShipNotification(GroupRole groupRole, boolean adding) {
        Date now = new Date();
        // for ISO1806 date format
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        StringBuilder xml = new StringBuilder("<Container><Patient><PatientNumbers>");

        if (groupRole.getUser().getIdentifiers() != null) {
            for (Identifier identifier : groupRole.getUser().getIdentifiers()) {
                xml.append("<PatientNumber><Number>");
                xml.append(identifier.getIdentifier());
                xml.append("</Number><Organization>");
                xml.append(identifier.getIdentifierType().getValue());
                xml.append("</Organization></PatientNumber>");
            }
        }

        xml.append("</PatientNumbers></Patient><ProgramMemberships><ProgramMembership><EnteredBy>");
        xml.append(getCurrentUser().getUsername());
        xml.append("</EnteredBy><EnteredAt>PV2</EnteredAt><EnteredOn>");
        xml.append(df.format(now));
        xml.append("</EnteredOn><ExternalId>");
        xml.append(groupRole.getId());
        xml.append("</ExternalId><ProgramName>");
        xml.append(groupRole.getGroup().getCode());
        xml.append("</ProgramName><ProgramDescription>");
        xml.append(groupRole.getGroup().getName());
        xml.append("</ProgramDescription><FromTime>");
        xml.append(df.format(groupRole.getCreated()));
        xml.append("</FromTime><ToTime>");
        if (!adding) {
            xml.append(df.format(now));
        }
        xml.append("</ToTime></ProgramMembership></ProgramMemberships></Container>");

        externalServiceService.addToQueue(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION, xml.toString(),
                getCurrentUser(), now);
    }

    @Override
    public void removeAllGroupRoles(Long userId) throws ResourceNotFoundException {
        groupRoleRepository.removeAllGroupRoles(findUser(userId));
    }

    private void deleteGroupRoleRelationship(Long userId, Long groupId, Long roleId, boolean checkGroupMembership)
            throws ResourceNotFoundException, ResourceForbiddenException {

        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException("Group not found");
        }

        // check if current user is a member of the group to be removed
        if (checkGroupMembership && !isUserMemberOfGroup(getCurrentUser(), entityGroup)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        User entityUser = userRepository.findOne(userId);
        if (entityUser == null) {
            throw new ResourceNotFoundException("User not found");
        }

        Role entityRole = roleRepository.findOne(roleId);
        if (entityRole == null) {
            throw new ResourceNotFoundException("Role not found");
        }

        GroupRole entityGroupRole = groupRoleRepository.findByUserGroupRole(entityUser, entityGroup, entityRole);
        if (entityGroupRole == null) {
            throw new ResourceNotFoundException("GroupRole not found");
        }

        groupRoleRepository.delete(entityGroupRole);

        // remove from user features if GP_MEDICATION group
        if (entityGroupRole.getGroup().getCode().equals(GpMedicationGroupCodes.ECS.toString())) {
            Feature entityFeature = featureRepository.findByName(FeatureType.GP_MEDICATION.toString());
            UserFeature userFeature = userFeatureRepository.findByUserAndFeature(entityUser, entityFeature);
            userFeatureRepository.delete(userFeature);
        }

        // audit
        boolean isPatient = entityRole.getRoleType().getValue().equals(RoleType.PATIENT);

        if (isPatient) {
            auditService.createAudit(AuditActions.PATIENT_GROUP_ROLE_DELETE, entityUser.getUsername(),
                    getCurrentUser(), userId, AuditObjectTypes.User, entityGroup);

            // send membership notification to RDC, not GroupTypes.SPECIALTY
            if (!entityGroupRole.getGroup().getGroupType().getValue().equals(GroupTypes.SPECIALTY.toString())) {
                sendGroupMemberShipNotification(entityGroupRole, false);
            }
        } else {
            auditService.createAudit(AuditActions.ADMIN_GROUP_ROLE_DELETE, entityUser.getUsername(),
                    getCurrentUser(), userId, AuditObjectTypes.User, entityGroup);
        }
    }

    private boolean groupRolesContainsGroup(Set<GroupRole> groupRoles, Group group) {
        for (GroupRole groupRole : groupRoles) {
            if (groupRole.getGroup().equals(group)) {
                return true;
            }
        }
        return false;
    }

    public Long add(User user) throws EntityExistsException {

        if (userRepository.usernameExistsCaseInsensitive(user.getUsername())) {
            throw new EntityExistsException("User already exists (username): " + user.getUsername());
        }

        User creator = getCurrentUser();
        user.setCreator(creator);
        // Everyone should change their password at login
        user.setChangePassword(Boolean.TRUE);
        user.setDeleted(Boolean.FALSE);

        // booleans
        if (user.getLocked() == null) {
            user.setLocked(false);
        }
        if (user.getEmailVerified() == null) {
            user.setEmailVerified(false);
        }
        if (user.getDummy() == null) {
            user.setDummy(false);
        }

        // forename and surname cannot be null (sometimes happens with migrated data)
        if (StringUtils.isEmpty(user.getForename())) {
            user.setForename("");
        }
        if (StringUtils.isEmpty(user.getSurname())) {
            user.setSurname("");
        }

        User newUser = userRepository.save(user);
        LOG.info("New user with id: {}, username: {}", user.getId(), user.getUsername());

        // check if patient
        boolean isPatient = false;

        if (!CollectionUtils.isEmpty(user.getGroupRoles())) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (roleRepository.findOne(groupRole.getRole().getId())
                        .getRoleType().getValue().equals(RoleType.PATIENT)) {
                    isPatient = true;
                }
            }
        }

        // audit creation
        if (isPatient) {
            auditService.createAudit(AuditActions.PATIENT_ADD, newUser.getUsername(), getCurrentUser(),
                    newUser.getId(), AuditObjectTypes.User, null);
        } else {
            auditService.createAudit(AuditActions.ADMIN_ADD, newUser.getUsername(), getCurrentUser(),
                    newUser.getId(), AuditObjectTypes.User, null);
        }

        if (!CollectionUtils.isEmpty(user.getGroupRoles())) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                // only save if group role doesn't already exist for this user
                if (!groupRoleRepository.userGroupRoleExists(
                        newUser.getId(), groupRole.getGroup().getId(), groupRole.getRole().getId())) {

                    groupRole.setUser(newUser);
                    groupRole.setCreator(creator);
                    groupRole = groupRoleRepository.save(groupRole);

                    if (isPatient) {
                        auditService.createAudit(AuditActions.PATIENT_GROUP_ROLE_ADD, newUser.getUsername(),
                                getCurrentUser(), newUser.getId(), AuditObjectTypes.User, groupRole.getGroup());
                    } else {
                        auditService.createAudit(AuditActions.ADMIN_GROUP_ROLE_ADD, newUser.getUsername(),
                                getCurrentUser(), newUser.getId(), AuditObjectTypes.User, groupRole.getGroup());
                    }

                    addParentGroupRoles(newUser.getId(), groupRole, creator, isPatient);
                }
            }
        }

        // Everyone should be in the generic group.
        addUserToGenericGroup(newUser, creator);

        if (!CollectionUtils.isEmpty(user.getUserFeatures())) {
            for (UserFeature userFeature : user.getUserFeatures()) {
                userFeature.setFeature(userFeature.getFeature());
                userFeature.setUser(newUser);
                userFeature.setCreator(creator);
                userFeatureRepository.save(userFeature);
            }
        }

        if (!CollectionUtils.isEmpty(user.getIdentifiers())) {
            for (Identifier identifier : user.getIdentifiers()) {
                identifier.setId(null);
                identifier.setUser(newUser);
                identifier.setCreator(creator);
                identifierRepository.save(identifier);
            }
        }

        return newUser.getId();
    }

    // We do this so early one gets the generic group
    private void addUserToGenericGroup(User user, User creator) {
        GroupRole groupRole = new GroupRole();
        groupRole.setUser(user);
        groupRole.setGroup(genericGroup);
        groupRole.setCreator(creator);
        groupRole.setRole(memberRole);
        groupRole.setStartDate(new Date());
        groupRoleRepository.save(groupRole);
    }

    /**
     * This persists the User map with GroupRoles and UserFeatures. The static
     * data objects are detached so have to be become managed again without updating the objects.
     *
     * @param user user to store
     * @return Long userId
     */
    public Long createUserWithPasswordEncryption(User user)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException {
        try {
            String salt = CommonUtils.generateSalt();
            user.setSalt(salt);
            user.setPassword(DigestUtils.sha256Hex(user.getPassword() + salt));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // validate that group roles exist and current user has rights to create
        for (GroupRole groupRole : user.getGroupRoles()) {
            if (!groupRepository.exists(groupRole.getGroup().getId())) {
                throw new ResourceNotFoundException("Group does not exist");
            }
            if (!isUserMemberOfGroup(getCurrentUser(), groupRepository.findOne(groupRole.getGroup().getId()))) {
                throw new ResourceForbiddenException("Forbidden");
            }
        }

        if (userRepository.usernameExistsCaseInsensitive(user.getUsername())) {
            throw new EntityExistsException("User already exists (username): " + user.getUsername());
        }

        if (userRepository.emailExists(user.getEmail())) {
            throw new EntityExistsException("User already exists (email): " + user.getEmail());
        }

        return add(user);
    }

    public User get(Long userId) throws ResourceNotFoundException {
        return findUser(userId);
    }

    public org.patientview.api.model.User getUser(Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User with this ID does not exist");
        }

        if (!currentUserCanGetUser(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        org.patientview.api.model.User transportUser = new org.patientview.api.model.User(user);

        // get last data received if present
        List<FhirLink> fhirLinks = fhirLinkRepository.findActiveByUser(user);
        if (!fhirLinks.isEmpty()) {
            transportUser.setLatestDataReceivedBy(new BaseGroup(fhirLinks.get(0).getGroup()));
            transportUser.setLatestDataReceivedDate(fhirLinks.get(0).getCreated());
        }

        return transportUser;
    }

    @Override
    public boolean currentUserCanGetUser(User user) {
        // if i am trying to access myself
        if (getCurrentUser().equals(user)) {
            return true;
        }

        // UNIT_ADMIN can get users from other groups (used when updating existing user)
        // as long as not GLOBAL_ADMIN or SPECIALTY_ADMIN
        if (Util.currentUserHasRole(RoleName.UNIT_ADMIN) || Util.currentUserHasRole(RoleName.GP_ADMIN)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getRole().getName().equals(RoleName.GLOBAL_ADMIN)
                        || groupRole.getRole().getName().equals(RoleName.SPECIALTY_ADMIN)) {
                    return false;
                }
            }

            return true;
        }

        // if i have staff group role in same groups
        for (GroupRole groupRole : user.getGroupRoles()) {
            if (isUserMemberOfGroup(getCurrentUser(), groupRole.getGroup())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean currentUserCanSwitchToUser(User user) {
        return userCanSwitchToUser(getCurrentUser(), user);
    }

    @Override
    public boolean userCanSwitchToUser(User user, User switchUser) {
        // if user trying to access themselves
        if (user.equals(switchUser)) {
            return true;
        }

        // if user trying to access a non patient user
        if (!isUserAPatient(switchUser)) {
            return false;
        }

        // if user has staff group role in same groups
        for (GroupRole groupRole : switchUser.getGroupRoles()) {
            if (isUserMemberOfGroup(user, groupRole.getGroup())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteFhirLinks(Long userId) {
        Set<Long> fhirLinkIdentifierIds = new HashSet<>();

        User user = userRepository.findOne(userId);
        if (user.getFhirLinks() != null) {
            for (FhirLink fhirLink : user.getFhirLinks()) {
                fhirLinkIdentifierIds.add(fhirLink.getIdentifier().getId());
                fhirLinkRepository.delete(fhirLink.getId());
            }

            for (Long id : fhirLinkIdentifierIds) {
                identifierRepository.delete(id);
            }
        }

        user.setFhirLinks(new HashSet<FhirLink>());
        userRepository.save(user);
    }

    @Override
    public org.patientview.api.model.User getByUsername(String username) {
        User foundUser = userRepository.findByUsernameCaseInsensitive(username);
        if (foundUser == null) {
            return null;
        } else {
            return new org.patientview.api.model.User(foundUser);
        }
    }

    @Override
    public User findByUsernameCaseInsensitive(String username) {
        return userRepository.findByUsernameCaseInsensitive(username);
    }

    @Override
    public org.patientview.api.model.User getByEmail(String email) {
        List<User> foundUsers = userRepository.findByEmail(email);

        // should only return one
        if (CollectionUtils.isEmpty(foundUsers)) {
            return null;
        } else {
            return new org.patientview.api.model.User(foundUsers.get(0));
        }
    }

    @Override
    public org.patientview.api.model.User getByIdentifierValue(String identifier) throws ResourceNotFoundException {
        List<Identifier> identifiers = identifierRepository.findByValue(identifier);
        if (CollectionUtils.isEmpty(identifiers)) {
            throw new ResourceNotFoundException("Identifier does not exist");
        }

        // assume identifiers are unique so get the user associated with the first identifier
        return new org.patientview.api.model.User(identifiers.get(0).getUser());
    }

    public void save(User user) throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {
        User entityUser = findUser(user.getId());
        String originalEmail = entityUser.getEmail();

        // don't allow setting username to same as other users
        org.patientview.api.model.User existingUser = getByUsername(user.getUsername());
        if (existingUser != null && !existingUser.getId().equals(entityUser.getId())) {
            throw new EntityExistsException("Username in use by another User");
        }

        boolean isPatient = false;
        boolean isLocked = user.getLocked() && !entityUser.getLocked();
        boolean isUnLocked = !user.getLocked() && entityUser.getLocked();

        for (GroupRole groupRole : entityUser.getGroupRoles()) {
            if (!groupRepository.exists(groupRole.getGroup().getId())) {
                throw new ResourceNotFoundException("Group does not exist");
            }

            Role role = roleRepository.findOne(groupRole.getRole().getId());
            if (!role.getName().equals(RoleName.MEMBER) && role.getRoleType().getValue().equals(RoleType.PATIENT)) {
                isPatient = true;
            }
        }

        if (!currentUserCanGetUser(entityUser)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        entityUser.setForename(user.getForename());
        entityUser.setSurname(user.getSurname());
        entityUser.setUsername(user.getUsername());
        entityUser.setEmail(user.getEmail());
        entityUser.setEmailVerified(user.getEmailVerified());
        entityUser.setLocked(user.getLocked());
        entityUser.setDummy(user.getDummy());
        entityUser.setContactNumber(user.getContactNumber());
        entityUser.setDateOfBirth(user.getDateOfBirth());
        entityUser.setRoleDescription(user.getRoleDescription());
        entityUser = userRepository.save(entityUser);

        // audit changed
        if (isPatient) {
            auditService.createAudit(AuditActions.PATIENT_EDIT, entityUser.getUsername(), getCurrentUser(),
                    entityUser.getId(), AuditObjectTypes.User, null);
        } else {
            auditService.createAudit(AuditActions.ADMIN_EDIT, entityUser.getUsername(), getCurrentUser(),
                    entityUser.getId(), AuditObjectTypes.User, null);
        }

        // audit locked or unlocked
        if (isLocked) {
            auditService.createAudit(AuditActions.ACCOUNT_LOCKED, entityUser.getUsername(), getCurrentUser(),
                    entityUser.getId(), AuditObjectTypes.User, null);
        }

        if (isUnLocked) {
            auditService.createAudit(AuditActions.ACCOUNT_UNLOCKED, entityUser.getUsername(), getCurrentUser(),
                    entityUser.getId(), AuditObjectTypes.User, null);
        }

        // audit email changed
        if (!user.getEmail().equals(originalEmail)) {
            auditService.createAudit(AuditActions.EMAIL_CHANGED, entityUser.getUsername(), getCurrentUser(),
                    entityUser.getId(), AuditObjectTypes.User, null);
        }
    }

    @Override
    public void updateOwnSettings(Long userId, User user)
            throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {
        User entityUser = findUser(user.getId());

        String originalEmail = entityUser.getEmail();

        entityUser.setEmail(user.getEmail());
        entityUser.setContactNumber(user.getContactNumber());
        entityUser.setForename(user.getForename());
        entityUser.setSurname(user.getSurname());
        userRepository.save(entityUser);

        // audit email changed
        if (!user.getEmail().equals(originalEmail)) {
            auditService.createAudit(AuditActions.EMAIL_CHANGED, entityUser.getUsername(), getCurrentUser(),
                    entityUser.getId(), AuditObjectTypes.User, null);
        }
    }

    private List<Long> getUserGroupIds() {
        List<org.patientview.api.model.Group> groups
                = groupService.getUserGroups(getCurrentUser().getId(), new GetParameters()).getContent();

        List<Long> groupIds = new ArrayList<>();
        for (org.patientview.api.model.Group group : groups) {
            groupIds.add(group.getId());
        }

        return groupIds;
    }

    /**
     * Get users based on a list of groups and roles
     * todo: fix this for PostgreSQL and hibernate nullhandling to avoid multiple queries
     *
     * @return Page of api User
     */
    public Page<org.patientview.api.model.User> getApiUsersByGroupsAndRoles(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {

        boolean andGroups = false;
        List<Long> groupIds = convertStringArrayToLongs(getParameters.getGroupIds());

        // if passed in single 0 for group Id then get all user's groups using OR, else use AND
        if (!CollectionUtils.isEmpty(groupIds)) {
            if (groupIds.size() == 1 && groupIds.get(0) == 0) {
                groupIds = getUserGroupIds();
            } else {
                // check current user is member of groups passed in
                for (Long groupId : groupIds) {
                    Group entityGroup = groupRepository.findOne(groupId);
                    if (entityGroup == null) {
                        throw new ResourceNotFoundException("Unknown Group");
                    }
                    if (!isUserMemberOfGroup(getCurrentUser(), entityGroup)) {
                        throw new ResourceForbiddenException("Forbidden");
                    }
                }
                andGroups = true;
            }
        } else {
            groupIds = getUserGroupIds();
        }

        // get pagination details, including sorting
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();

        PageRequest pageable;
        Integer pageConverted = (StringUtils.isNotEmpty(page)) ? Integer.parseInt(page) : 0;
        Integer sizeConverted = (StringUtils.isNotEmpty(size)) ? Integer.parseInt(size) : Integer.MAX_VALUE;

        // todo: it is the Sort.NullHandling.NULLS_FIRST etc that is not picked up by hibernate
        if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortDirection)) {
            if (sortDirection.equals("DESC")) {
                pageable = new PageRequest(pageConverted, sizeConverted,
                        new Sort(new Sort.Order(Sort.Direction.DESC, sortField, Sort.NullHandling.NULLS_FIRST)));
            } else {
                pageable = new PageRequest(pageConverted, sizeConverted,
                        new Sort(new Sort.Order(Sort.Direction.ASC, sortField, Sort.NullHandling.NULLS_LAST)));
            }
        } else {
            pageable = new PageRequest(pageConverted, sizeConverted);
        }

        // handle searching by field (username, forename, surname, identifier, email)
        String searchUsername = getParameters.getSearchUsername();
        searchUsername = StringUtils.isEmpty(searchUsername) ? "%%" : "%" + searchUsername.trim().toUpperCase() + "%";
        String searchForename = getParameters.getSearchForename();
        searchForename = StringUtils.isEmpty(searchForename) ? "%%" : "%" + searchForename.trim().toUpperCase() + "%";
        String searchSurname = getParameters.getSearchSurname();
        searchSurname = StringUtils.isEmpty(searchSurname) ? "%%" : "%" + searchSurname.trim().toUpperCase() + "%";
        String searchIdentifier = getParameters.getSearchIdentifier();
        searchIdentifier
                = StringUtils.isEmpty(searchIdentifier) ? "%%" : "%" + searchIdentifier.trim().toUpperCase() + "%";
        String searchEmail = getParameters.getSearchEmail();
        searchEmail = StringUtils.isEmpty(searchEmail) ? "%%" : "%" + searchEmail.trim().toUpperCase() + "%";

        // isolate into either staff or patient queries (staff do not consider identifier)
        boolean staff = false;
        boolean patient = false;

        List<Role> allRoles = Util.convertIterable(roleRepository.findAll());
        Map<Long, Role> roleMap = new HashMap<>();
        for (Role role : allRoles) {
            roleMap.put(role.getId(), role);
        }

        List<Long> roleIds = convertStringArrayToLongs(getParameters.getRoleIds());
        for (Long roleId : roleIds) {
            if (roleMap.get(roleId).getRoleType().getValue().equals(RoleType.STAFF)) {
                staff = true;
            } else if (roleMap.get(roleId).getRoleType().getValue().equals(RoleType.PATIENT)) {
                patient = true;
            }
        }

        StatusFilter statusFilter = null;

        // get status filter for filtering users by status (e.g. locked, active, inactive)
        if (Util.isInEnum(getParameters.getStatusFilter(), StatusFilter.class)) {
            statusFilter = StatusFilter.valueOf(getParameters.getStatusFilter());
        }

        // Todo: improve this when a more recent Hibernate fixes Sort.NullHandling for PostgreSQL
        // This avoids the default setting of NULL in PostgreSQL being larger than any value when sorting
        // Note: this is not optimal (two queries) but is due to Hibernate not considering Sort.NullHandling with the
        // PostgreSQL dialect (see commented out code in PostgresCustomDialect.java)
        StringBuilder sql = new StringBuilder();
        sql.append("FROM User u ");
        sql.append("JOIN u.groupRoles gr ");
        if (!staff && patient) {
            sql.append("JOIN u.identifiers i ");
        }
        sql.append("WHERE gr.role.id IN :roleIds ");
        sql.append("AND gr.group.id IN :groupIds ");
        sql.append("AND (UPPER(u.username) LIKE :searchUsername) ");
        sql.append("AND (UPPER(u.forename) LIKE :searchForename) ");
        sql.append("AND (UPPER(u.surname) LIKE :searchSurname) ");
        sql.append("AND (UPPER(u.email) LIKE :searchEmail) ");
        if (!staff && patient) {
            sql.append("AND (i IN (SELECT id FROM Identifier id WHERE UPPER(id.identifier) LIKE :searchIdentifier)) ");
        }
        sql.append("AND u.deleted = false ");

        // locked users
        if (statusFilter != null && statusFilter.equals(StatusFilter.LOCKED)) {
            sql.append("AND u.locked = true ");
        }

        boolean dateRange = false;

        // active users (INACTIVE_MONTH_LIMIT months)
        if (statusFilter != null && statusFilter.equals(StatusFilter.ACTIVE)) {
            sql.append("AND u.lastLogin BETWEEN :startDate AND :endDate ");
            dateRange = true;
        }

        // inactive users (INACTIVE_MONTH_LIMIT months)
        if (statusFilter != null && statusFilter.equals(StatusFilter.INACTIVE)) {
            sql.append("AND (u.lastLogin NOT BETWEEN :startDate AND :endDate OR u.lastLogin = NULL) ");
            dateRange = true;
        }

        StringBuilder sortOrder = new StringBuilder();

        if (StringUtils.isNotEmpty(sortField) && StringUtils.isNotEmpty(sortDirection)) {
            sortOrder.append("ORDER BY ");
            boolean fieldExists = false;
            boolean fieldIsString = false;

            // check type of field by name (e.g. surname = String)
            Field[] fields = User.class.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equalsIgnoreCase(sortField)) {
                    fieldExists = true;
                    if (f.getType().equals(String.class)) {
                        fieldIsString = true;
                    }
                }
            }

            if (!fieldExists) {
                throw new ResourceNotFoundException("Incorrect search field");
            }

            // make ordering case insensitive
            if (fieldIsString) {
                sortOrder.append("UPPER(u.");
                sortOrder.append(sortField);
                sortOrder.append(") ");
            } else {
                sortOrder.append("u.");
                sortOrder.append(sortField);
            }

            sortOrder.append(" ");
            sortOrder.append(sortDirection);

            if (sortDirection.equals("DESC")) {
                sortOrder.append(" NULLS LAST");
            } else {
                sortOrder.append(" NULLS FIRST");
            }
        }

        StringBuilder userListSql = new StringBuilder("SELECT u ");
        // todo: heavy query, needs rewriting to be a count, difficult with JPA using HAVING clause
        StringBuilder userCountSql = new StringBuilder("SELECT u.id ");

        userListSql.append(sql);
        userListSql.append(" GROUP BY u.id ");
        userCountSql.append(sql);
        userCountSql.append(" GROUP BY u.id ");

        // Updated as when more than 1 identifier, the patient was excluded
        // TODO Use more efficient method of checking (Distinct count isn't efficient)
        if (andGroups) {
            if (patient) {
                userListSql.append("HAVING COUNT(gr) = :groupCount * COUNT(DISTINCT i) ");
                userCountSql.append("HAVING COUNT(gr) = :groupCount * COUNT(DISTINCT i) ");
            } else {
                userListSql.append("HAVING COUNT(gr) = :groupCount ");
                userCountSql.append("HAVING COUNT(gr) = :groupCount ");
            }
        }

        userListSql.append(sortOrder);

        Query query = entityManager.createQuery(userListSql.toString());
        Query countQuery = entityManager.createQuery(userCountSql.toString());

        query.setParameter("searchUsername", searchUsername);
        query.setParameter("searchForename", searchForename);
        query.setParameter("searchSurname", searchSurname);
        query.setParameter("searchEmail", searchEmail);
        if (!staff && patient) {
            query.setParameter("searchIdentifier", searchIdentifier);
        }
        query.setParameter("groupIds", groupIds);
        query.setParameter("roleIds", roleIds);

        countQuery.setParameter("searchUsername", searchUsername);
        countQuery.setParameter("searchForename", searchForename);
        countQuery.setParameter("searchSurname", searchSurname);
        countQuery.setParameter("searchEmail", searchEmail);
        if (!staff && patient) {
            countQuery.setParameter("searchIdentifier", searchIdentifier);
        }
        countQuery.setParameter("groupIds", groupIds);
        countQuery.setParameter("roleIds", roleIds);

        if (andGroups) {
            query.setParameter("groupCount", Long.valueOf(groupIds.size()));
            countQuery.setParameter("groupCount", Long.valueOf(groupIds.size()));
        }

        query.setMaxResults(sizeConverted);

        if (pageConverted == 0) {
            query.setFirstResult(0);
        } else {
            query.setFirstResult((sizeConverted * (pageConverted + 1)) - sizeConverted);
        }

        if (dateRange) {
            DateTime now = new DateTime();
            DateTime startDate = now.minusMonths(INACTIVE_MONTH_LIMIT);
            query.setParameter("startDate", startDate.toDate());
            query.setParameter("endDate", now.toDate());
            countQuery.setParameter("startDate", startDate.toDate());
            countQuery.setParameter("endDate", now.toDate());
        }

        List<org.patientview.api.model.User> transportContent = convertUsersToTransportUsers(query.getResultList());
        return new PageImpl<>(transportContent, pageable, countQuery.getResultList().size());
    }

    /**
     * Get users based on a list of groups and roles (only used by conversation service now)
     *
     * @return Page of standard User
     */
    public Page<User> getUsersByGroupsAndRolesNoFilter(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {

        List<Long> groupIds = convertStringArrayToLongs(getParameters.getGroupIds());

        // check current user is member of groups passed in
        for (Long groupId : groupIds) {
            Group entityGroup = groupRepository.findOne(groupId);
            if (entityGroup == null) {
                throw new ResourceNotFoundException("Unknown Group");
            }
        }

        List<Long> roleIds = convertStringArrayToLongs(getParameters.getRoleIds());
        String size = getParameters.getSize();
        String page = getParameters.getPage();
        String sortField = getParameters.getSortField();
        String sortDirection = getParameters.getSortDirection();

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

        // isolate into either staff, patient or both queries (staff or patient much quicker as no outer join)
        boolean staff = false;
        boolean patient = false;

        List<Role> allRoles = Util.convertIterable(roleRepository.findAll());
        Map<Long, Role> roleMap = new HashMap<>();
        for (Role role : allRoles) {
            roleMap.put(role.getId(), role);
        }

        for (Long roleId : roleIds) {
            if (roleMap.get(roleId).getRoleType().getValue().equals(RoleType.STAFF)) {
                staff = true;
            } else if (roleMap.get(roleId).getRoleType().getValue().equals(RoleType.PATIENT)) {
                patient = true;
            }
        }

        if (!staff && patient) {
            return userRepository.findPatientByGroupsRolesNoFilter(groupIds, roleIds, pageable);
        }

        if (staff && !patient) {
            return userRepository.findStaffByGroupsRolesNoFilter(groupIds, roleIds, pageable);
        }

        throw new ResourceNotFoundException("No Users found");
    }

    /**
     * Get users based on a list of groups, roles and user features
     *
     * @return Page of standard User
     */
    public Page<User> getUsersByGroupsRolesFeatures(GetParameters getParameters) throws ResourceNotFoundException {

        List<Long> groupIds = convertStringArrayToLongs(getParameters.getGroupIds());
        List<Long> roleIds = convertStringArrayToLongs(getParameters.getRoleIds());
        List<Long> featureIds = convertStringArrayToLongs(getParameters.getFeatureIds());
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

        // isolate into either staff, patient or both queries (staff or patient much quicker as no outer join)
        boolean staff = false;
        boolean patient = false;

        List<Role> allRoles = Util.convertIterable(roleRepository.findAll());
        Map<Long, Role> roleMap = new HashMap<>();
        for (Role role : allRoles) {
            roleMap.put(role.getId(), role);
        }

        for (Long roleId : roleIds) {
            if (roleMap.get(roleId).getRoleType().getValue().equals(RoleType.STAFF)) {
                staff = true;
            } else if (roleMap.get(roleId).getRoleType().getValue().equals(RoleType.PATIENT)) {
                patient = true;
            }
        }

        if (!staff && patient) {
            return userRepository.findPatientByGroupsRolesFeatures(filterText, groupIds, roleIds, featureIds, pageable);
        }

        if (staff) {
            return userRepository.findStaffByGroupsRolesFeatures(filterText, groupIds, roleIds, featureIds, pageable);
        }

        throw new ResourceNotFoundException("No Users found");
    }

    public void delete(Long userId, boolean forceDelete)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

        User user = findUser(userId);
        boolean isPatient = false;
        String username = user.getUsername();

        if (currentUserCanGetUser(user)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                Role role = roleRepository.findOne(groupRole.getRole().getId());
                if (!role.getName().equals(RoleName.MEMBER) && role.getRoleType().getValue().equals(RoleType.PATIENT)) {
                    isPatient = true;
                }

                // audit removal (apart from MEMBER)
                if (!role.getName().equals(RoleName.MEMBER) && isPatient) {
                    auditService.createAudit(AuditActions.PATIENT_GROUP_ROLE_DELETE, user.getUsername(),
                            getCurrentUser(), userId, AuditObjectTypes.User, groupRole.getGroup());
                }
            }

            if (isUserAPatient(user)) {
                isPatient = true;
            }

            // wipe patient and observation data if it exists
            if (!CollectionUtils.isEmpty(user.getFhirLinks())) {
                patientService.deleteExistingPatientData(user.getFhirLinks());
                patientService.deleteAllExistingObservationData(user.getFhirLinks());
            }

            if (isPatient || forceDelete) {
                // patient, delete from conversations and associated messages, other non user tables
                conversationService.deleteUserFromConversations(user);
                auditService.deleteUserFromAudit(user);
                userTokenRepository.deleteByUserId(user.getId());
                userMigrationRepository.deleteByUserId(user.getId());
                userObservationHeadingRepository.deleteByUserId(user.getId());
                alertRepository.deleteByUserId(user.getId());
                deleteFhirLinks(user.getId());
                userRepository.delete(user);
            } else {
                // staff member, mark as deleted
                user.setDeleted(true);
                userRepository.save(user);
            }
        } else {
            throw new ResourceForbiddenException("Forbidden");
        }

        // audit deletion
        AuditActions auditActions;
        if (isPatient) {
            auditActions = AuditActions.PATIENT_DELETE;
        } else {
            auditActions = AuditActions.ADMIN_DELETE;
        }

        auditService.createAudit(auditActions, username, getCurrentUser(), userId, AuditObjectTypes.User, null);
    }

    /**
     * Reset the flag so the user will not be prompted to change the password again
     *
     * @param userId   Id of User to change password
     * @param password password to set
     */
    public void changePassword(Long userId, String password) throws ResourceNotFoundException {
        User user = findUser(userId);
        try {
            user.setChangePassword(Boolean.FALSE);
            String salt = CommonUtils.generateSalt();
            user.setSalt(salt);
            user.setPassword(DigestUtils.sha256Hex(password + salt));
            user.setLocked(Boolean.FALSE);
            user.setFailedLogonAttempts(0);
            userRepository.save(user);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * On a password reset the user should change on login
     */
    public org.patientview.api.model.User resetPassword(Long userId, String password)
            throws ResourceNotFoundException, ResourceForbiddenException, MessagingException {
        User user = findUser(userId);

        if (!currentUserCanGetUser(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        // only send email if verified
        if (user.getEmailVerified()) {
            try {
                emailService.sendEmail(getPasswordResetEmail(user, password));
            } catch (MessagingException | MailException me) {
                LOG.error("Could not send reset password email {}", me);
            }
        }
        try {
            String salt = CommonUtils.generateSalt();
            user.setSalt(salt);
            user.setPassword(DigestUtils.sha256Hex(password + salt));
            user.setChangePassword(Boolean.TRUE);
            user.setLocked(Boolean.FALSE);
            user.setFailedLogonAttempts(0);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new org.patientview.api.model.User(userRepository.save(user));
    }

    /**
     * Send a email to the user email address to verify have access to the email account
     */
    public Boolean sendVerificationEmail(Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException, MailException, MessagingException {
        User user = findUser(userId);

        if (!currentUserCanGetUser(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }
        user.setVerificationCode(CommonUtils.getAuthToken());
        userRepository.save(user);

        return emailService.sendEmail(getVerifyEmailEmail(user));
    }

    public Boolean verify(Long userId, String verificationCode)
            throws ResourceNotFoundException, VerificationException {
        User user = findUser(userId);
        if (user.getVerificationCode().equals(verificationCode)) {
            user.setEmailVerified(true);
            userRepository.save(user);
            return true;
        } else {
            throw new VerificationException("Verification code does not match");
        }
    }

    public void addFeature(Long userId, Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User user = findUser(userId);
        Feature feature = featureRepository.findOne(featureId);
        if (feature == null) {
            throw new ResourceForbiddenException("Feature not found");
        }

        if (!currentUserCanGetUser(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        UserFeature userFeature = new UserFeature();
        userFeature.setFeature(feature);
        userFeature.setUser(user);
        userFeature.setCreator(userRepository.findOne(getCurrentUser().getId()));
        userFeatureRepository.save(userFeature);
    }

    public void deleteFeature(Long userId, Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User user = findUser(userId);
        Feature feature = featureRepository.findOne(featureId);
        if (feature == null) {
            throw new ResourceForbiddenException("Feature not found");
        }

        if (!currentUserCanGetUser(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        userFeatureRepository.delete(userFeatureRepository.findByUserAndFeature(user, feature));
    }

    // Stage 1 of Forgotten Password, user knows username and email
    public void resetPasswordByUsernameAndEmail(String username, String email)
            throws ResourceNotFoundException, MailException, MessagingException {

        LOG.info("Forgotten password (username, email) for " + username);
        User user = userRepository.findByUsernameCaseInsensitive(username);
        if (user == null) {
            throw new ResourceNotFoundException("Could not find account");
        }

        if (user.getEmail().equalsIgnoreCase(email)) {
            user.setChangePassword(Boolean.TRUE);

            // Set the new password
            String password = CommonUtils.generatePassword();

            // email the user but ignore if exception and log
            try {
                emailService.sendEmail(getPasswordResetEmail(user, password));
            } catch (MailException | MessagingException me) {
                LOG.error("Cannot send email: {}", me);
            }

            try {
                String salt = CommonUtils.generateSalt();
                user.setSalt(salt);

                // Hash the password and save user
                user.setLocked(Boolean.FALSE);
                user.setFailedLogonAttempts(0);
                user.setPassword(DigestUtils.sha256Hex(password + salt));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            userRepository.save(user);
        } else {
            throw new ResourceNotFoundException("Could not find account");
        }

        auditService.createAudit(AuditActions.PASSWORD_RESET_FORGOTTEN, user.getUsername(),
                user, user.getId(), AuditObjectTypes.User, null);
    }

    public void addInformation(Long userId, List<UserInformation> userInformation) throws ResourceNotFoundException {
        User user = findUser(userId);

        // for user information we want to update existing info, only create if doesn't already exist
        for (UserInformation newUserInformation : userInformation) {
            UserInformation entityUserInformation
                    = userInformationRepository.findByUserAndType(user, newUserInformation.getType());
            if (entityUserInformation != null) {
                entityUserInformation.setValue(newUserInformation.getValue());
                userInformationRepository.save(entityUserInformation);
            } else {
                if (newUserInformation.getValue() != null) {
                    newUserInformation.setUser(user);
                    newUserInformation.setCreator(getCurrentUser());
                    userInformationRepository.save(newUserInformation);
                }
            }
        }
    }

    @Override
    public void addOtherUsersInformation(Long userId, List<UserInformation> userInformation)
            throws ResourceNotFoundException {

        User user = findUser(userId);

        // for user information we want to update existing info, only create if doesn't already exist
        for (UserInformation newUserInformation : userInformation) {
            UserInformation entityUserInformation
                    = userInformationRepository.findByUserAndType(user, newUserInformation.getType());
            if (entityUserInformation != null) {
                entityUserInformation.setValue(newUserInformation.getValue());
                userInformationRepository.save(entityUserInformation);
            } else {
                if (newUserInformation.getValue() != null) {
                    newUserInformation.setUser(user);
                    newUserInformation.setCreator(getCurrentUser());
                    userInformationRepository.save(newUserInformation);
                }
            }
        }
    }

    public List<UserInformation> getInformation(Long userId) throws ResourceNotFoundException {
        User user = findUser(userId);
        return userInformationRepository.findByUser(user);
    }

    private List<org.patientview.api.model.User> convertUsersToTransportUsers(List<User> users) {
        List<org.patientview.api.model.User> transportUsers = new ArrayList<>();

        for (User user : users) {
            transportUsers.add(new org.patientview.api.model.User(user));
        }

        return transportUsers;
    }

    private boolean isUserAPatient(User user) {

        for (GroupRole groupRole : user.getGroupRoles()) {
            if (!groupRole.getRole().getRoleType().getValue().equals(RoleType.PATIENT)) {
                return false;
            }
        }

        return true;
    }

    private User findUser(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException(String.format("Could not find user %s", userId));
        }
        return user;
    }

    private Email getPasswordResetEmail(User user, String password) {
        Email email = new Email();
        email.setSenderEmail(properties.getProperty("smtp.sender.email"));
        email.setSenderName(properties.getProperty("smtp.sender.name"));
        email.setRecipients(new String[]{user.getEmail()});
        email.setSubject("PatientView - Your Password Has Been Reset");

        StringBuilder sb = new StringBuilder();
        sb.append("Dear ");
        sb.append(user.getForename());
        sb.append(" ");
        sb.append(user.getSurname());
        sb.append(", <br/><br/>Your password on <a href=\"");
        sb.append(properties.getProperty("site.url"));
        sb.append("\">PatientView</a> ");
        sb.append("has been reset. Your new password is: <br/><br/>");
        sb.append(password);
        email.setBody(sb.toString());

        return email;
    }

    private Email getVerifyEmailEmail(User user) {
        Email email = new Email();
        email.setSenderEmail(properties.getProperty("smtp.sender.email"));
        email.setSenderName(properties.getProperty("smtp.sender.name"));
        email.setSubject("PatientView - Please Verify Your Account");
        email.setRecipients(new String[]{user.getEmail()});

        StringBuilder sb = new StringBuilder();
        sb.append("Dear ");
        sb.append(user.getForename());
        sb.append(" ");
        sb.append(user.getSurname());
        sb.append(", <br/><br/>Please <a href=\"");
        sb.append(properties.getProperty("site.url"));
        sb.append("/#/verify?userId=");
        sb.append(user.getId());
        sb.append("&verificationCode=");
        sb.append(user.getVerificationCode());
        sb.append("\">click here</a> to validate the email address associated with your account on PatientView.");
        email.setBody(sb.toString());

        return email;
    }

    public Group getGenericGroup() {
        return genericGroup;
    }

    public void setGenericGroup(Group genericGroup) {
        this.genericGroup = genericGroup;
    }

    public Role getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(Role memberRole) {
        this.memberRole = memberRole;
    }

    @Override
    public String addPicture(Long userId, MultipartFile file) throws ResourceInvalidException {
        User user = userRepository.findOne(userId);
        String fileName = "";

        try {
            fileName = file.getOriginalFilename();
            byte[] inputBytes = file.getBytes();
            if (inputBytes == null || inputBytes.length == 0) {
                throw new ResourceInvalidException("Failed to upload " + fileName + ": empty");
            }

            InputStream inputStream = new ByteArrayInputStream(inputBytes);
            BufferedImage bufferedImage = ImageIO.read(inputStream);

            // detect orientation if set in exif (ipad etc) and rotate image then resize to MAXIMUM_IMAGE_WIDTH
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());
            ExifIFD0Directory exifDirectory = metadata.getDirectory(ExifIFD0Directory.class);

            if (exifDirectory != null && exifDirectory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = exifDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                LOG.info("" + orientation);
                switch (orientation) {
                    case THREE:
                        bufferedImage = transformImage(bufferedImage, ONE_HUNDRED_AND_EIGHTY);
                        break;
                    case SIX:
                        bufferedImage = transformImage(bufferedImage, NINETY);
                        break;
                    case EIGHT:
                        bufferedImage = transformImage(bufferedImage, TWO_HUNDRED_AND_SEVENTY);
                        break;
                    default:
                        bufferedImage = transformImage(bufferedImage, 0);
                        break;
                }
            } else {
                bufferedImage = transformImage(bufferedImage, 0);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream);
            byte[] outputBytes = byteArrayOutputStream.toByteArray();
            String outputBase64 = new String(Base64.encodeBase64(outputBytes));
            user.setPicture(outputBase64);
            userRepository.save(user);

            inputStream.close();
            byteArrayOutputStream.close();
            return "Uploaded '" + fileName + "' (" + outputBytes.length + " bytes, " + outputBase64.length() + " char)";
        } catch (Exception e) {
            throw new ResourceInvalidException("Failed to upload " + fileName + ": " + e.getMessage());
        }
    }

    @Override
    public void addPicture(Long userId, String base64) {
        User user = userRepository.findOne(userId);
        user.setPicture(base64);
        userRepository.save(user);
    }

    protected BufferedImage transformImage(BufferedImage image, int angle) {
        Double aspect = Double.valueOf(image.getHeight()) / Double.valueOf(image.getWidth());
        int width = image.getWidth();
        int height = image.getHeight();
        AffineTransform transform = new AffineTransform();

        if (angle == NINETY) {
            transform.rotate(Math.toRadians(angle), width / 2 * aspect, height / 2);
            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
            image = op.filter(image, null);

            if (width * aspect > MAXIMUM_IMAGE_WIDTH) {
                width = MAXIMUM_IMAGE_WIDTH;
                Double heightDouble = MAXIMUM_IMAGE_WIDTH / aspect;
                height = heightDouble.intValue();
            }
        } else if (angle == ONE_HUNDRED_AND_EIGHTY) {
            transform.rotate(Math.toRadians(angle), width / 2, height / 2);
            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
            image = op.filter(image, null);

            if (width > MAXIMUM_IMAGE_WIDTH) {
                width = MAXIMUM_IMAGE_WIDTH;
                Double heightDouble = MAXIMUM_IMAGE_WIDTH * aspect;
                height = heightDouble.intValue();
            }
        } else if (angle == TWO_HUNDRED_AND_SEVENTY) {
            image = transformImage(image, ONE_HUNDRED_AND_EIGHTY);
            image = transformImage(image, NINETY);
            Double widthDouble = image.getWidth() * aspect;
            width = widthDouble.intValue();
            height = image.getHeight();
        } else {
            if (width > MAXIMUM_IMAGE_WIDTH) {
                width = MAXIMUM_IMAGE_WIDTH;
                Double heightDouble = MAXIMUM_IMAGE_WIDTH * aspect;
                height = heightDouble.intValue();
            }
        }

        Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage bufferedScaledImage = new BufferedImage(scaledImage.getWidth(null),
                scaledImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        bufferedScaledImage.getGraphics().drawImage(scaledImage, 0, 0, null);

        return bufferedScaledImage;
    }

    @Override
    public byte[] getPicture(Long userId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        if (!currentUserCanGetUser(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }
        if (StringUtils.isNotEmpty(user.getPicture())) {
            return Base64.decodeBase64(user.getPicture());
        } else {
            return null;
        }
    }

    @Override
    public boolean usernameExists(String username) {
        if (StringUtils.isEmpty(username)) {
            return false;
        }

        return (userRepository.findByUsernameCaseInsensitive(username) != null);
    }

    @Override
    public void deletePicture(Long userId) throws ResourceNotFoundException {
        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        user.setPicture(null);
        userRepository.save(user);
    }

    @Override
    public void moveUsersGroup(final Long groupFromId, final Long groupToId, final Long roleId,
        final boolean checkParentGroup) throws ResourceForbiddenException, ResourceNotFoundException {

        // check all exist
        final Group groupFrom = groupRepository.findOne(groupFromId);
        final Group groupTo = groupRepository.findOne(groupToId);
        final Role role = roleRepository.findOne(roleId);

        if (groupFrom == null) {
            throw new ResourceNotFoundException("Moving Users: Group with id " + groupFromId + " does not exist");
        }
        if (groupTo == null) {
            throw new ResourceNotFoundException("Moving Users: Group with id " + groupToId + " does not exist");
        }
        if (role == null) {
            throw new ResourceNotFoundException("Moving Users: Role with id " + roleId + " does not exist");
        }

        String[] groupFromIds = {groupFromId.toString()};
        String[] groupToIds = {groupToId.toString()};
        String[] roleIds = {roleId.toString()};

        GetParameters getParametersFrom = new GetParameters();
        getParametersFrom.setGroupIds(groupFromIds);
        getParametersFrom.setRoleIds(roleIds);

        GetParameters getParametersTo = new GetParameters();
        getParametersTo.setGroupIds(groupToIds);
        getParametersTo.setRoleIds(roleIds);

        Page<User> usersFrom = getUsersByGroupsAndRolesNoFilter(getParametersFrom);
        Page<User> usersTo = getUsersByGroupsAndRolesNoFilter(getParametersTo);

        int count = 0;
        int countDelete = 0;

        LOG.info("Moving Users: Moving " + usersFrom.getContent().size() + " users with Role '"
                + role.getName().toString() + "' from Group with code '"
                + groupFrom.getCode() + "' to '" + groupTo.getCode() + "'");

        for (User user : usersFrom.getContent()) {
            if (checkParentGroup) {
                // method using full add and remove group role, including parent groups

                LOG.info("Moving Users: Deleting GroupRole '" + groupFrom.getCode() + ", "
                        + role.getName().toString() + "' for user '" + user.getUsername() + "'");
                deleteGroupRole(user.getId(), groupFromId, roleId);
                countDelete++;

                if (!usersTo.getContent().contains(user)) {
                    LOG.info("Moving Users: Adding GroupRole '" + groupTo.getCode() + ", "
                            + role.getName().toString() + "'  for user '" + user.getUsername() + "'");
                    addGroupRole(user.getId(), groupToId, roleId);
                    count++;
                }
            } else {
                // alternate method using direct GroupRole modification
                GroupRole entityGroupRole = groupRoleRepository.findByUserGroupRole(user, groupFrom, role);
                if (entityGroupRole != null) {
                    LOG.info("Moving Users: Deleting GroupRole '" + groupFrom.getCode() + ", "
                            + role.getName().toString() + "' for user '" + user.getUsername() + "'");
                    groupRoleRepository.delete(entityGroupRole);
                    countDelete++;
                }

                if (!usersTo.getContent().contains(user)) {
                    LOG.info("Moving Users: Adding GroupRole '" + groupTo.getCode() + ", "
                            + role.getName().toString() + "'  for user '" + user.getUsername() + "'");
                    GroupRole newGroupRole = new GroupRole(user, groupTo, role);
                    groupRoleRepository.save(newGroupRole);
                    count++;
                }
            }
        }

        LOG.info("Moving Users: Moved " + countDelete + " users with Role '"
                + role.getName().toString()
                + "' from Group with code '" + groupFrom.getCode() + "' to '" + groupTo.getCode() + "'");
        LOG.info("Moving Users: " + countDelete + " deleted GroupRole");
        LOG.info("Moving Users: " + count + " added GroupRole");
        LOG.info("Moving Users: " + (countDelete - count) + " already in new group");
    }
}
