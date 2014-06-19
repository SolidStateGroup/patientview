package org.patientview.api.service.util;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;

import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
public final class TestUtils {

    private TestUtils() {

    }

    public static User createUser(String name) {
        User user = new User();
        user.setId(897L);
        user.setUsername(name);
        user.setChangePassword(Boolean.FALSE);
        user.setLocked(Boolean.FALSE);
        user.setStartDate(new Date());
        user.setName(name);
        user.setEmail("test@patientview.org");
        return user;
    }


    public static Role createRole(String name) {
        Role role = new Role();
        role.setId(734L);
        role.setName(name);
        role.setCreated(new Date());
        role.setCreator(createUser("roleCreator"));
        return role;
    }


    public static Feature createFeature(String name) {
        Feature feature = new Feature();
        feature.setId(144L);
        feature.setName(name);
        feature.setCreated(new Date());
        feature.setCreator(createUser("featureCreator"));
        return feature;
    }

    public static Group createGroup(String name) {
        Group group = new Group();
        group.setId(13L);
        group.setName(name);
        group.setCreated(new Date());
        group.setCreator(createUser("groupCreator"));
        return group;
    }

    public static GroupRole createGroupRole(String roleName, String groupName, User user) {
        GroupRole groupRole = new GroupRole();
        Group group = TestUtils.createGroup(groupName);
        groupRole.setGroup(group);
        Role role = TestUtils.createRole(roleName);
        groupRole.setUser(user);
        groupRole.setRole(role);

        return groupRole;
    }

    public static UserFeature createUserFeature(String featureName, User user) {
        UserFeature userFeature = new UserFeature();
        userFeature.setId(567L);
        Feature feature = TestUtils.createFeature(featureName);
        userFeature.setFeature(feature);
        userFeature.setUser(user);
        return userFeature;

    }

}
