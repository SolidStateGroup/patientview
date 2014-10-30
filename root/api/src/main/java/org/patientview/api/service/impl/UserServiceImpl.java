package org.patientview.api.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.patientview.api.model.Email;
import org.patientview.api.service.EmailService;
import org.patientview.api.service.FhirLinkService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.ObservationService;
import org.patientview.api.service.PatientService;
import org.patientview.api.service.UserService;
import org.patientview.api.util.Util;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserFeatureRepository;
import org.patientview.persistence.repository.UserInformationRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Service
@Transactional
public class UserServiceImpl extends AbstractServiceImpl<UserServiceImpl> implements UserService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupService groupService;

    @Inject
    private PatientService patientService;

    @Inject
    private FhirLinkService fhirLinkService;

    @Inject
    private FeatureRepository featureRepository;

    @Inject
    private GroupRoleRepository groupRoleRepository;

    @Inject
    private RoleRepository roleRepository;

    @Inject
    private UserInformationRepository userInformationRepository;

    @Inject
    private UserFeatureRepository userFeatureRepository;

    @Inject
    private IdentifierRepository identifierRepository;

    @Inject
    private FhirLinkRepository fhirLinkRepository;

    @Inject
    private EmailService emailService;

    @Inject
    private ObservationService observationService;

    @Inject
    private ObservationHeadingService observationHeadingService;

    @Inject
    private FhirResource fhirResource;

    @Inject
    private Properties properties;

    // TODO make these value configurable
    private static final Long GENERIC_ROLE_ID = 7L;
    private static final Long GENERIC_GROUP_ID = 1L;
    private Group genericGroup;
    private Role memberRole;

    // used during migration
    private HashMap<String, ObservationHeading> observationHeadingsMap;

    @PostConstruct
    public void init() {
        // set up generic groups
        memberRole = roleRepository.findOne(GENERIC_ROLE_ID);
        genericGroup = groupRepository.findOne(GENERIC_GROUP_ID);
    }

    private void addParentGroupRoles(GroupRole groupRole, User creator) {

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
        if (!isCurrentUserMemberOfGroup(group)) {
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
        addParentGroupRoles(groupRole, creator);
        return groupRole;
    }

    public void deleteGroupRole(Long userId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        deleteGroupRoleRelationship(userId, groupId, roleId);

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
                    groupRole.getRole().getId());
        }
    }

    @Override
    public void removeAllGroupRoles(Long userId) throws ResourceNotFoundException {
        groupRoleRepository.removeAllGroupRoles(findUser(userId));
    }

    private void deleteGroupRoleRelationship(Long userId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        Group entityGroup = groupRepository.findOne(groupId);
        if (entityGroup == null) {
            throw new ResourceNotFoundException("Group not found");
        }

        // check if current user is a member of the group to be removed
        if (!isCurrentUserMemberOfGroup(entityGroup)) {
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
    }

    private boolean groupRolesContainsGroup(Set<GroupRole> groupRoles, Group group) {
        for (GroupRole groupRole : groupRoles) {
            if (groupRole.getGroup().equals(group)) {
                return true;
            }
        }
        return false;
    }

    public Long add(User user) {

        User creator = getCurrentUser();
        user.setCreator(creator);
        // Everyone should change their password at login
        user.setChangePassword(Boolean.TRUE);

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

        if (!CollectionUtils.isEmpty(user.getGroupRoles())) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                // only save if group role doesn't already exist for this user
                if (!groupRoleRepository.userGroupRoleExists(
                        newUser.getId(), groupRole.getGroup().getId(), groupRole.getRole().getId())) {

                    groupRole.setGroup(groupRole.getGroup());
                    groupRole.setRole(groupRole.getRole());
                    groupRole.setUser(newUser);
                    groupRole.setCreator(creator);
                    groupRole = groupRoleRepository.save(groupRole);
                    addParentGroupRoles(groupRole, creator);
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
        user.setPassword(DigestUtils.sha256Hex(user.getPassword()));

        // validate that group roles exist and current user has rights to create
        for (GroupRole groupRole : user.getGroupRoles()) {
            if (!groupRepository.exists(groupRole.getGroup().getId())) {
                throw new ResourceNotFoundException("Group does not exist");
            }
            if (!isCurrentUserMemberOfGroup(groupRole.getGroup())) {
                throw new ResourceForbiddenException("Forbidden");
            }
        }

        if (userRepository.usernameExists(user.getUsername())) {
            throw new EntityExistsException("User already exists (username)");
        }

        if (userRepository.emailExists(user.getEmail())) {
            throw new EntityExistsException("User already exists (email)");
        }

        return add(user);
    }

    // migration Only
    public Long migrateUser(MigrationUser migrationUser) throws EntityExistsException, ResourceNotFoundException {

        if (userRepository.usernameExists(migrationUser.getUser().getUsername())) {
            throw new EntityExistsException("User already exists (username)");
        }

        // add basic user object
        User user = migrationUser.getUser();
        Long userId = add(user);

        //return userId;

        //User entityUser = userRepository.findOne(userId);
        //Identifier identifier = entityUser.getIdentifiers().iterator().next();

        //Patient patient = patientService.buildPatient(entityUser, identifier);

        //testing
        /*try {
            JSONObject fhirPatient = new JSONObject();
            for (int i=0;i<10;i++) {
                fhirPatient = fhirResource.create(patient);
                //wait(10L);
                //JSONObject fhirPatient2 = fhirResource.create(patient);
            }

            ObservationHeading observationHeading = observationHeadingService.findAll().get(0);
            FhirObservation fhirObservation = migrationUser.getObservations().get(0);


            FhirLink fhirLink = new FhirLink();
            fhirLink.setUser(entityUser);
            fhirLink.setIdentifier(identifier);
            fhirLink.setGroup(entityUser.getGroupRoles().iterator().next().getGroup());
            fhirLink.setResourceId(getResourceId(fhirPatient));
            fhirLink.setVersionId(getVersionId(fhirPatient));
            fhirLink.setResourceType(ResourceType.Patient.name());
            fhirLink.setActive(true);

            Observation observation
                    = observationService.buildObservation(fhirObservation, observationHeading, fhirLink);
            fhirResource.create(observation);

        } catch (Exception e) {
            LOG.error("Could not migrate patient data, " + e.toString());
        }*/

        // only patients will have observations
        if (!CollectionUtils.isEmpty(migrationUser.getObservations())) {
            try {
                migratePatientData(userId, migrationUser);
            } catch (FhirResourceException fre) {
                LOG.error("Could not migrate patient data: {}", fre);
            }
        }

        LOG.info("{} Done", userId);
        return userId;
    }

    // migration only
    private void migratePatientData(Long userId, MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, FhirResourceException {

        try {
            User entityUser = userRepository.findOne(userId);
            LOG.info(userId + " has " + migrationUser.getObservations().size() + " Observations");

            Set<FhirLink> fhirLinks = entityUser.getFhirLinks();

            // set up map of observation headings
            if (observationHeadingsMap == null) {
                observationHeadingsMap = new HashMap<>();
                for (ObservationHeading observationHeading : observationHeadingService.findAll()) {
                    observationHeadingsMap.put(observationHeading.getCode().toUpperCase(), observationHeading);
                }
            }

            // set up map of identifiers
            HashMap<String, Identifier> identifierMap = new HashMap<>();
            for (Identifier identifier : entityUser.getIdentifiers()) {
                identifierMap.put(identifier.getIdentifier(), identifier);
            }

            for (ObservationHeading observationHeading : observationHeadingService.findAll()) {
                observationHeadingsMap.put(observationHeading.getCode().toUpperCase(), observationHeading);
            }

            if (fhirLinks == null) {
                fhirLinks = new HashSet<>();
            }

            for (FhirObservation fhirObservation : migrationUser.getObservations()) {

                // get identifier for this user
                Identifier identifier = identifierMap.get(fhirObservation.getIdentifier());

                // get observation heading for this observation
                ObservationHeading observationHeading = observationHeadingsMap.get(fhirObservation.getName().toUpperCase());

                //LOG.info("1 " + new Date().getTime());

                if (identifier != null && observationHeading != null) {
                    FhirLink fhirLink = getFhirLink(fhirObservation.getGroup(), fhirObservation.getIdentifier(), fhirLinks);

                    if (fhirLink == null) {
                        // create new FHIR Patient
                        Patient patient = patientService.buildPatient(entityUser, identifier);
                        JSONObject fhirPatient = fhirResource.create(patient);

                        // create new FhirLink and add to list of known fhirlinks
                        fhirLink = new FhirLink();
                        fhirLink.setUser(entityUser);
                        fhirLink.setIdentifier(identifier);
                        fhirLink.setGroup(fhirObservation.getGroup());
                        fhirLink.setResourceId(getResourceId(fhirPatient));
                        fhirLink.setVersionId(getVersionId(fhirPatient));
                        fhirLink.setResourceType(ResourceType.Patient.name());
                        fhirLink.setActive(true);
                        fhirLinks.add(fhirLinkService.save(fhirLink));
                    }

                    //LOG.info("2 " + new Date().getTime());
                    Observation observation
                            = observationService.buildObservation(fhirObservation, observationHeading, fhirLink);
                    fhirResource.create(observation);
                    //observationService.addObservation(fhirObservation, observationHeading, fhirLink);
                    //LOG.info("3 " + new Date().getTime());
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    private FhirLink getFhirLink(Group group, String identifierText, Set<FhirLink> fhirLinks) {
        for (FhirLink fhirLink : fhirLinks) {
            if (fhirLink.getGroup().equals(group) && fhirLink.getIdentifier().getIdentifier().equals(identifierText)) {
                return fhirLink;
            }
        }
        return null;
    }

    private UUID getVersionId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        JSONArray links = (JSONArray) resource.get("link");
        JSONObject link = (JSONObject)  links.get(0);
        String[] href = link.getString("href").split("/");
        return UUID.fromString(href[href.length - 1]);
    }

    private UUID getResourceId(final JSONObject bundle) {
        JSONArray resultArray = (JSONArray) bundle.get("entry");
        JSONObject resource = (JSONObject) resultArray.get(0);
        return UUID.fromString(resource.get("id").toString());
    }

    // not used
    public User get(Long userId) throws ResourceNotFoundException {
        return findUser(userId);
    }

    public org.patientview.api.model.User getUser(Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User user = userRepository.findOne(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User with this ID does not exist");
        }

        if (!canGetUser(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        org.patientview.api.model.User transportUser = new org.patientview.api.model.User(user, null);

        // get last data received if present
        List<FhirLink> fhirLinks = fhirLinkRepository.findActiveByUser(user);
        if (!fhirLinks.isEmpty()) {
            transportUser.setLatestDataReceivedBy(new org.patientview.api.model.Group(fhirLinks.get(0).getGroup()));
            transportUser.setLatestDataReceivedDate(fhirLinks.get(0).getCreated());
        }

        return transportUser;
    }

    private boolean canGetUser(User user) {
        // if i am trying to access myself
        if (getCurrentUser().equals(user)) {
            return true;
        }

        // UNIT_ADMIN can get users from other groups (used when updating existing user) as long as not GLOBAL_ADMIN
        // or SPECIALTY_ADMIN
        if (Util.doesContainRoles(RoleName.UNIT_ADMIN)) {
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
            if (isCurrentUserMemberOfGroup(groupRole.getGroup())) {
                return true;
            }
        }
        return false;
    }

    public org.patientview.api.model.User getByUsername(String username) {
        User foundUser = userRepository.findByUsername(username);
        if (foundUser == null) {
            return null;
        } else {
            return new org.patientview.api.model.User(foundUser, null);
        }
    }
    public org.patientview.api.model.User getByEmail(String email) {
        User foundUser = userRepository.findByEmail(email);
        if (foundUser == null) {
            return null;
        } else {
            return new org.patientview.api.model.User(foundUser, null);
        }
    }

    public void save(User user) throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {
        User entityUser = findUser(user.getId());

        // don't allow setting username to same as other users
        org.patientview.api.model.User existingUser = getByUsername(user.getUsername());
        if (existingUser != null && !existingUser.getId().equals(entityUser.getId())) {
            throw new EntityExistsException("Username in use by another User");
        }

        for (GroupRole groupRole : entityUser.getGroupRoles()) {
            if (!groupRepository.exists(groupRole.getGroup().getId())) {
                throw new ResourceNotFoundException("Group does not exist");
            }
        }

        if (!canGetUser(entityUser)) {
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
        userRepository.save(entityUser);
    }

    private List<org.patientview.api.model.User> convertUsersToTransportUsers(List<User> users) {
        List<org.patientview.api.model.User> transportUsers = new ArrayList<>();

        for (User user : users) {
            // if patient, add patient specific FHIR details
            Set<FhirLink> fhirLinks = user.getFhirLinks();
            if (fhirLinks.isEmpty()) {
                transportUsers.add(new org.patientview.api.model.User(user, null));
            } else {
                // is a patient (has FHIR content), get most recent FHIR data and populate transport object
                FhirLink recentFhirData = fhirLinks.iterator().next();
                for (FhirLink fhirLink : fhirLinks) {
                    if (fhirLink.getCreated().after(recentFhirData.getCreated())) {
                        recentFhirData = fhirLink;
                    }
                }

                try {
                    Patient fhirPatient = patientService.get(recentFhirData.getResourceId());
                    transportUsers.add(new org.patientview.api.model.User(user, fhirPatient));
                } catch (FhirResourceException fre) {
                    LOG.error("FhirResourceException on retrieving patient data");
                    transportUsers.add(new org.patientview.api.model.User(user, null));
                }
            }
        }

        return transportUsers;
    }

    /**
     * Get users based on a list of groups and roles
     * @return Page of api User
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

    /**
     * Get users based on a list of groups, roles and user features
     * @return Page of api User
     */
    public Page<org.patientview.api.model.User> getUsersByGroupsRolesFeatures(GetParameters getParameters) {

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

        Page<User> users = userRepository.findByGroupsRolesFeatures(filterText, groupIds, roleIds
                , featureIds, pageable);

        // convert to lightweight transport objects, create Page and return
        List<org.patientview.api.model.User> transportContent = convertUsersToTransportUsers(users.getContent());
        return new PageImpl<>(transportContent, pageable, users.getTotalElements());
    }

    public void delete(Long userId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = findUser(userId);
        if (canGetUser(user)) {
            userRepository.delete(user);
        }
    }

    /**
     * Reset the flag so the user will not be prompted to change the password again
     *
     * @param userId Id of User to change password
     * @param password password to set
     */
    public void changePassword(Long userId, String password) throws ResourceNotFoundException {
        User user = findUser(userId);
        user.setChangePassword(Boolean.FALSE);
        user.setPassword(DigestUtils.sha256Hex(password));
        userRepository.save(user);
    }

    /**
     * On a password reset the user should change on login
     */
    public org.patientview.api.model.User resetPassword(Long userId, String password)
            throws ResourceNotFoundException, ResourceForbiddenException {
        User user = findUser(userId);

        if (!canGetUser(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        user.setChangePassword(Boolean.TRUE);
        user.setPassword(DigestUtils.sha256Hex(password));
        return new org.patientview.api.model.User(userRepository.save(user), null);
    }

    /**
     * Send a email to the user email address to verify have access to the email account
     */
    public Boolean sendVerificationEmail(Long userId) throws ResourceNotFoundException, ResourceForbiddenException {
        User user = findUser(userId);

        if (!canGetUser(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

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

    public void addFeature(Long userId, Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException {

        User user = findUser(userId);
        Feature feature = featureRepository.findOne(featureId);
        if (feature == null) {
            throw new ResourceForbiddenException("Feature not found");
        }

        if (!canGetUser(user)) {
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

        if (!canGetUser(user)) {
            throw new ResourceForbiddenException("Forbidden");
        }

        userFeatureRepository.delete(userFeatureRepository.findByUserAndFeature(user, feature));
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
            user.setPassword(CommonUtils.getAuthToken());
            emailService.sendEmail(getForgottenPassword(user));
            // Hash the password
            user.setPassword(DigestUtils.sha256Hex(user.getPassword()));

            userRepository.save(user);
        } else {
            throw new ResourceNotFoundException("Could not find account");
        }

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
                    userInformationRepository.save(newUserInformation);
                }
            }
        }
    }

    public List<UserInformation> getInformation(Long userId) throws ResourceNotFoundException {
        User user = findUser(userId);
        return userInformationRepository.findByUser(user);
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
