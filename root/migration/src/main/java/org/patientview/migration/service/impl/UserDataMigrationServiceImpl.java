package org.patientview.migration.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.patientview.Group;
import org.patientview.GroupRole;
import org.patientview.Identifier;
import org.patientview.Role;
import org.patientview.User;
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
import org.patientview.repository.UserDao;
import org.patientview.repository.UserMappingDao;
import org.patientview.service.UserManager;
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
    private UserMappingDao userMappingDao;

    @Inject
    private AdminDataMigrationService adminDataMigrationService;

    @Inject
    private SpecialtyUserRoleDao specialtyUserRoleDao;

    @Inject
    private UserManager userManager;


    public void migrate() {

        for (org.patientview.patientview.model.User oldUser : userDao.getAll()) {


            boolean isPatient = false;
            Set<String> nhsNumbers = new HashSet<String>();

            User newUser = createUser(oldUser);

            // create the User on the new system
            Long userId = callApiCreateUser(newUser);

            for (UserMapping userMapping : userMappingDao.getAll(oldUser.getUsername())) {


                // We do want the patient group.
                if (!userMapping.getUnitcode().equalsIgnoreCase("PATIENT")) {
                    nhsNumbers.add(userMapping.getNhsno());
                    Role patientRole = adminDataMigrationService.getRoleByName(Roles.PATIENT);

                    if (StringUtils.isNotEmpty(userMapping.getNhsno())) {

                        Group patientGroup = adminDataMigrationService.getGroupByCode(userMapping.getUnitcode());

                        if (newUser != null && userId != null && patientGroup != null && patientRole != null) {
                            callApiAddGroupRole(userId, patientGroup.getId(), patientRole.getId());
                        }

                        isPatient = true;


                    } else {

                        Role role = null;
                        List<SpecialtyUserRole> specialtyUserRoles = specialtyUserRoleDao.get(oldUser);
                        /// TODO try and fix this - get the first role and apply it to all group (no group role mapping)
                        if (CollectionUtils.isNotEmpty(specialtyUserRoles)) {


                            String roleName = specialtyUserRoles.get(0).getRole(); //FIXME hack from original PV
                            if (roleName.equals("unitadmin")) {
                                role = adminDataMigrationService.getRoleByName(Roles.UNIT_ADMIN);
                            }
                            if (roleName.equals("unitstaff")) {
                                role = adminDataMigrationService.getRoleByName(Roles.STAFF_ADMIN);
                            }


                            Group group = adminDataMigrationService.getGroupByCode(userMapping.getUnitcode());
                            if (newUser != null && userId != null && group != null && role != null) {
                                callApiAddGroupRole(userId, group.getId(), role.getId());
                            }
                        }

                    }
                }

            }

            if (isPatient && userId != null) {
                addSpecialty(oldUser, userId);
                addIdentifier(userId, nhsNumbers);
            }
        }

    }

    public void bulkUserCreate(String unitCode, Long count, Roles roleName) {
        Date now = new Date();

        Group userUnit = adminDataMigrationService.getGroupByCode(unitCode);
        Role userRole = adminDataMigrationService.getRoleByName(roleName);

        if (userUnit != null && userRole != null) {

            for (Long i = now.getTime(); i<now.getTime() + count; i++) {

                // create user and store
                MigrationUser newUser = new MigrationUser();
                newUser.setForename(i.toString());
                newUser.setSurname(i.toString());
                newUser.setChangePassword(true);
                newUser.setPassword("pppppp");
                newUser.setLocked(false);
                newUser.setDummy(true);
                newUser.setFailedLogonAttempts(0);
                newUser.setEmail("patientview" + i.toString() + "@solidstategroup.com");
                newUser.setUsername(i.toString());
                newUser.setEmailVerified(false);
                newUser.setIdentifiers(new HashSet<Identifier>());

                // if role is Roles.PATIENT add identifier
                if (roleName.equals(Roles.PATIENT)) {
                    Identifier identifier = new Identifier();
                    identifier.setIdentifier(i.toString());
                    identifier.setIdentifierType(adminDataMigrationService.getLookupByName("CHI_NUMBER"));
                    newUser.getIdentifiers().add(identifier);
                }

                Group group = new Group();
                group.setId(userUnit.getId());
                Role role = new Role();
                role.setId(userRole.getId());
                GroupRole groupRole = new GroupRole();
                groupRole.setGroup(group);
                groupRole.setRole(role);
                newUser.setGroupRoles(new HashSet<GroupRole>());
                newUser.getGroupRoles().add(groupRole);

                callApiMigrateUser(newUser);
            }
        }
    }

    private void addIdentifier(Long userId, Set<String> nhsNumbers) {
        for (String nhsNUmber : nhsNumbers) {
                Identifier identifier = new Identifier();
                identifier.setIdentifier(nhsNUmber);
                identifier.setIdentifierType(adminDataMigrationService.getLookupByName("NHS_NUMBER"));
                callApiAddIdentifier(identifier, userId);
        }
    }

    private void addSpecialty(org.patientview.patientview.model.User user, Long userId) {

        List<Group> groups = getUserSpecialty(user);
        for (Group group : groups) {
            Role role = adminDataMigrationService.getRoleByName(Roles.PATIENT);
            if (userId != null && group != null && role != null) {
                callApiAddGroupRole(userId, group.getId(), role.getId());
            }
        }

    }

    private User callApiGetUser(String username) {

        String url = JsonUtil.pvUrl + "/user/username?username=" + username;
        try {
            User user = JsonUtil.jsonRequest(url, User.class, null, HttpPut.class);
            LOG.info("Found user");
            return user;
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to find user");
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to find user");
        } catch (Exception e) {
            LOG.error("Unable to find user");
            e.printStackTrace();
        }

        return null;
    }

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

    private Identifier callApiAddIdentifier(Identifier identifier, Long userId) {

        String url = JsonUtil.pvUrl + "/user/" + userId + "/identifiers";
        try {
            Identifier newIdentifier = JsonUtil.jsonRequest(url, Identifier.class, identifier, HttpPost.class);
            LOG.info("Added Identifier");
            return newIdentifier;
        } catch (JsonMigrationException jme) {
            LOG.error("Unable to add identifier");
        } catch (JsonMigrationExistsException jee) {
            LOG.error("Unable to add identifier");
        } catch (Exception e) {
            LOG.error("Unable to add identifier");
            e.printStackTrace();
        }

        return null;
    }

    // testing for bulk user creation
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

    private Long callApiCreateUser(User user) {
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
            newUser.setEmail("Unknown@patientview.org");
        } else{
            newUser.setEmail(user.getEmail());
        }
        newUser.setUsername(user.getUsername());
        newUser.setEmailVerified(user.isEmailverified());
        return newUser;
    }

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



}
