package org.patientview.migration.service.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.patientview.Group;
import org.patientview.GroupRole;
import org.patientview.User;
import org.patientview.migration.service.AdminDataMigrationService;
import org.patientview.migration.service.UserDataMigrationService;
import org.patientview.migration.util.JsonUtil;
import org.patientview.patientview.model.SpecialtyUserRole;
import org.patientview.patientview.model.UserMapping;
import org.patientview.repository.SpecialtyUserRoleDao;
import org.patientview.repository.UserDao;
import org.patientview.repository.UserMappingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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

    public void migrate() {

        for (org.patientview.patientview.model.User oldUser : userDao.getAll()) {

            User newUser = createUser(oldUser);

            for (UserMapping userMapping : userMappingDao.getAll(oldUser.getUsername())) {

                if (StringUtils.isNotEmpty(userMapping.getNhsno())) {

                    if (newUser.getGroupRoles() == null) {
                        newUser.setGroupRoles(new ArrayList<GroupRole>());
                    }

                    GroupRole userGroup = new GroupRole();
                    userGroup.setGroup(adminDataMigrationService.getGroupByCode(userMapping.getUnitcode()));
                    userGroup.setRole(adminDataMigrationService.getRoleByName("PATIENT"));
                    newUser.getGroupRoles().add(userGroup);
                }

            }

           /* for (Group group : getUserSpecialty(oldUser)) {

                    if (newUser.getGroupRoles() == null) {
                        newUser.setGroupRoles(new ArrayList<GroupRole>());
                    }

                    GroupRole userGroup = new GroupRole();
                    userGroup.setGroup(group);
                    userGroup.setRole(adminDataService.getRoleByName("PATIENT"));
                    newUser.getGroupRoles().add(userGroup);

            }*/

            String url = JsonUtil.pvUrl + "/user";
            newUser = JsonUtil.jsonRequest(url, User.class, newUser, HttpPost.class);

            if (newUser != null) {
                LOG.info("Create user: {}", newUser.getUsername());
            } else {
                LOG.error("Unable to create user: {}", oldUser.getUsername());
            }

        }

    }


    public void migratePatientUser() {
        for (org.patientview.patientview.model.User oldUser : userDao.getAll()) {

            User newUser = createUser(oldUser);

            for (UserMapping userMapping : userMappingDao.getAll(oldUser.getUsername())) {

                if (StringUtils.isNotEmpty(userMapping.getNhsno())) {

                    if (newUser.getGroupRoles() == null) {
                        newUser.setGroupRoles(new ArrayList<GroupRole>());
                    }

                    GroupRole userGroup = new GroupRole();
                    userGroup.setGroup(adminDataMigrationService.getGroupByCode(userMapping.getUnitcode()));
                    userGroup.setRole(adminDataMigrationService.getRoleByName("PATIENT"));
                    newUser.getGroupRoles().add(userGroup);
                }

            }

            String url = JsonUtil.pvUrl + "/user";
            newUser = JsonUtil.jsonRequest(url, User.class, newUser, HttpPost.class);

            if (newUser != null) {
                LOG.info("Create user: {}", newUser.getUsername());
            } else {
                LOG.error("Unable to create user: {}", oldUser.getUsername());
            }

        }



    }

    public User createUser(org.patientview.patientview.model.User user) {
        User newUser = new User();
        newUser.setName(user.getFirstName() + user.getLastName());
        newUser.setChangePassword(user.isFirstlogon());
        newUser.setPassword(user.getPassword());
        newUser.setLocked(user.isAccountlocked());
        if (StringUtils.isEmpty(user.getEmail())) {
            newUser.setEmail("Unknown@patientview.org");
        } else{
            newUser.setEmail(user.getEmail());
        }
        newUser.setUsername(user.getUsername());
        return newUser;
    }

    public List<Group> getUserSpecialty(org.patientview.patientview.model.User  oldUser) {

        List<Group> groups = new ArrayList<Group>();

        for (SpecialtyUserRole specialtyUserRole : specialtyUserRoleDao.get(oldUser)) {

            if (specialtyUserRole.getSpecialty().getContext().equalsIgnoreCase("ibd")) {
                groups.add(adminDataMigrationService.getIbd());
            }

            if (specialtyUserRole.getSpecialty().getContext().equalsIgnoreCase("renal")) {
                groups.add(adminDataMigrationService.getRenal());
            }

            if (specialtyUserRole.getSpecialty().getContext().equalsIgnoreCase("diabetes")) {
                groups.add(adminDataMigrationService.getDiabetes());
            }
        }

        return groups;

    }



}
