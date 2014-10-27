package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.patientview.Feature;
import org.patientview.Group;
import org.patientview.GroupRole;
import org.patientview.Identifier;
import org.patientview.Lookup;
import org.patientview.Role;
import org.patientview.User;
import org.patientview.UserFeature;
import org.patientview.enums.FeatureType;
import org.patientview.enums.IdentifierTypes;
import org.patientview.enums.Roles;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.migration.util.exception.JsonMigrationExistsException;
import org.patientview.model.MigrationUser;
import org.patientview.patientview.model.SpecialtyUserRole;
import org.patientview.patientview.model.UserMapping;
import org.patientview.repository.SpecialtyUserRoleDao;
import org.patientview.repository.TestResultDao;
import org.patientview.repository.UserDao;
import org.patientview.repository.UserMappingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@Service
public class UserDataMigrationServiceImpl implements UserDataMigrationService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDataMigrationServiceImpl.class);

    @Inject
    private UserDao userDao;

    @Inject
    private TestResultDao testResultDao;

    @Inject
    private UserMappingDao userMappingDao;

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @Inject
    private SpecialtyUserRoleDao specialtyUserRoleDao;

    public void migrate() {

        Role patientRole = adminDataMigrationService.getRoleByName(Roles.PATIENT);
        Lookup nhsNumberIdentifier = adminDataMigrationService.getLookupByName(IdentifierTypes.NHS_NUMBER.toString());

        for (org.patientview.patientview.model.User oldUser : userDao.getAll()) {
            Set<String> identifiers = new HashSet<String>();

            // basic user information
            User newUser = createUser(oldUser);

            for (UserMapping userMapping : userMappingDao.getAll(oldUser.getUsername())) {
                if (!userMapping.getUnitcode().equalsIgnoreCase("PATIENT") && newUser != null) {
                    if (StringUtils.isNotEmpty(userMapping.getNhsno())) {
                        identifiers.add(userMapping.getNhsno());

                        // add group (specialty is added automatically when creating user within a UNIT group)
                        Group group = adminDataMigrationService.getGroupByCode(userMapping.getUnitcode());

                        if (group != null && patientRole != null) {
                            GroupRole groupRole = new GroupRole();
                            groupRole.setGroup(group);
                            groupRole.setRole(patientRole);
                            newUser.getGroupRoles().add(groupRole);
                        }
                    } else {
                        Role role = null;
                        List<SpecialtyUserRole> specialtyUserRoles = specialtyUserRoleDao.get(oldUser);
                        // TODO: try and fix this - get the first role and apply it to all group (no group role mapping)
                        // TODO: required hack from original PatientView
                        if (CollectionUtils.isNotEmpty(specialtyUserRoles)) {
                            String roleName = specialtyUserRoles.get(0).getRole();

                            if (roleName.equals("unitadmin")) {
                                role = adminDataMigrationService.getRoleByName(Roles.UNIT_ADMIN);
                            }
                            if (roleName.equals("unitstaff")) {
                                role = adminDataMigrationService.getRoleByName(Roles.STAFF_ADMIN);
                            }

                            // add group (specialty is added automatically when creating user within a UNIT group)
                            Group group = adminDataMigrationService.getGroupByCode(userMapping.getUnitcode());

                            if (group != null && role != null) {
                                GroupRole groupRole = new GroupRole();
                                groupRole.setGroup(group);
                                groupRole.setRole(patientRole);
                                newUser.getGroupRoles().add(groupRole);
                            }
                        }
                    }
                }
            }

            if (newUser != null) {
                // identifiers (will only be for patient)
                for (String identifierText : identifiers) {
                    Identifier identifier = new Identifier();
                    identifier.setIdentifier(identifierText);
                    identifier.setIdentifierType(nhsNumberIdentifier);
                    newUser.getIdentifiers().add(identifier);
                }

                // features
                if (oldUser.isIsrecipient()) {
                    Feature feature = adminDataMigrationService.getFeatureByName(FeatureType.MESSAGING.toString());
                    if (feature != null) {
                        newUser.getUserFeatures().add(new UserFeature(feature));
                    }
                }
                if (oldUser.isFeedbackRecipient()) {
                    Feature feature = adminDataMigrationService.getFeatureByName(FeatureType.MESSAGING.toString());
                    if (feature != null) {
                        newUser.getUserFeatures().add(new UserFeature(feature));
                    }
                }

                // convert to transport object
                MigrationUser migrationUser = new MigrationUser(newUser);

                // call REST to store migrated user
                callApiMigrateUser(migrationUser);
            }
        }
    }

    public void bulkUserCreate(String unitCode, Long count, Roles roleName) {
        Date now = new Date();

        Group userUnit = adminDataMigrationService.getGroupByCode(unitCode);
        Role userRole = adminDataMigrationService.getRoleByName(roleName);

        if (userUnit != null && userRole != null) {

            for (Long i = now.getTime(); i<now.getTime() + count; i++) {

                // create user
                User newUser = new User();
                newUser.setForename("test" + i.toString());
                newUser.setSurname("test");
                newUser.setChangePassword(true);
                newUser.setPassword("pppppp");
                newUser.setLocked(false);
                newUser.setDummy(true);
                newUser.setFailedLogonAttempts(0);
                newUser.setEmail("test" + i.toString() + "@solidstategroup.com");
                newUser.setEmailVerified(false);
                newUser.setUsername(i.toString());
                newUser.setIdentifiers(new HashSet<Identifier>());

                // if role is Roles.PATIENT add identifier
                if (roleName.equals(Roles.PATIENT)) {
                    Identifier identifier = new Identifier();
                    identifier.setIdentifier(i.toString());
                    identifier.setIdentifierType(adminDataMigrationService.getLookupByName("NHS_NUMBER"));
                    newUser.getIdentifiers().add(identifier);
                }

                // add group role (specialty is added automatically when creating user within a UNIT group)
                Group group = new Group();
                group.setId(userUnit.getId());
                Role role = new Role();
                role.setId(userRole.getId());
                GroupRole groupRole = new GroupRole();
                groupRole.setGroup(group);
                groupRole.setRole(role);
                newUser.setGroupRoles(new HashSet<GroupRole>());
                newUser.getGroupRoles().add(groupRole);

                // add user feature (usually for staff)
                newUser.setUserFeatures(new HashSet<UserFeature>());
                UserFeature userFeature = new UserFeature();
                userFeature.setFeature(adminDataMigrationService.getFeatureByName(FeatureType.MESSAGING.toString()));
                newUser.getUserFeatures().add(userFeature);

                MigrationUser migrationUser = new MigrationUser(newUser);

                // call REST to store migrated patient
                callApiMigrateUser(migrationUser);
            }
        }
    }


    private Long callApiMigrateUser(MigrationUser user) {
        String url = JsonUtil.pvUrl + "/user/migrate";
        Long userId = null;
        try {
            userId = JsonUtil.jsonRequest(url, Long.class, user, HttpPost.class);
            LOG.info("Created user: {} with id {}", user.getUsername(), userId);
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to create user: {}", user.getUsername());
        } catch (JsonMigrationExistsException jee) {
            LOG.info("User {} already exists", user.getUsername());
        }

        return userId;
    }

    public User createUser(org.patientview.patientview.model.User user) {
        User newUser = new User();
        newUser.setForename(user.getFirstName());
        newUser.setSurname(user.getLastName());
        newUser.setChangePassword(user.isFirstlogon());
        newUser.setPassword(user.getPassword());
        newUser.setLocked(user.isAccountlocked());
        newUser.setDummy(user.isDummypatient());
        newUser.setFailedLogonAttempts(user.getFailedlogons());

        if (StringUtils.isEmpty(user.getEmail())) {
            newUser.setEmail("unknown@patientview.org");
        } else{
            newUser.setEmail(user.getEmail());
        }
        newUser.setUsername(user.getUsername());
        newUser.setEmailVerified(user.isEmailverified());

        newUser.setIdentifiers(new HashSet<Identifier>());
        newUser.setGroupRoles(new HashSet<GroupRole>());
        newUser.setUserFeatures(new HashSet<UserFeature>());

        return newUser;
    }

    // deprecated as specialty added automatically for child groups during user creation
    public List<Group> getUserSpecialty(org.patientview.patientview.model.User oldUser) {

        List<Group> groups = new ArrayList<Group>();

        for (SpecialtyUserRole specialtyUserRole : specialtyUserRoleDao.get(oldUser)) {

            if (specialtyUserRole.getSpecialty().getContext().equalsIgnoreCase("ibd")) {
                groups.add(adminDataMigrationService.getGroupByName("idb"));
            }

            if (specialtyUserRole.getSpecialty().getContext().equalsIgnoreCase("renal")) {
                groups.add(adminDataMigrationService.getGroupByName("renal"));
            }

            if (specialtyUserRole.getSpecialty().getContext().equalsIgnoreCase("diabetes")) {
                groups.add(adminDataMigrationService.getGroupByName("diabetes"));
            }
        }

        return groups;
    }

    // deprecated as specialty added automatically for child groups during user creation
    private void addSpecialty(org.patientview.patientview.model.User user, Long userId) {

        List<Group> groups = getUserSpecialty(user);
        for (Group group : groups) {
            Role role = adminDataMigrationService.getRoleByName(Roles.PATIENT);
            if (userId != null && group != null && role != null) {
                callApiAddGroupRole(userId, group.getId(), role.getId());
            }
        }
    }

    // deprecated as specialty added automatically for child groups during user creation
    private void callApiAddGroupRole(Long userId, Long groupId, Long roleId) {
        String url = JsonUtil.pvUrl + "/user/" + userId + "/group/" + groupId + "/role/" + roleId;
        try {
            JsonUtil.jsonRequest(url, GroupRole.class, null, HttpPut.class);
            LOG.info("Created group and role for user");
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to add user to group");
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to add user to group");
        } catch (Exception e) {
            LOG.error("Unable to add group role");
            e.printStackTrace();
        }
    }
}
