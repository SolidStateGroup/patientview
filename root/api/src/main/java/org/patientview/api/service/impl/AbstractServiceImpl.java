package org.patientview.api.service.impl;

import org.apache.commons.lang.ArrayUtils;
import org.patientview.api.util.ApiUtil;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created to group the services add utilities
 *
 * Created by james@solidstategroup.com
 * Created on 05/08/2014
 */
public abstract class AbstractServiceImpl<T extends AbstractServiceImpl> {

    protected final Logger LOG = LoggerFactory.getLogger(getServiceClass());

    private Class<T> getServiceClass()  {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) superclass.getActualTypeArguments()[0];
    }

    protected static <T> List<T> convertIterable(Iterable<T> iterable) {
        return Util.convertIterable(iterable);
    }

    protected List<Long> convertStringArrayToLongs(String[] strings) {
        final List<Long> longs = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(strings)) {
            for (String string : strings) {
                if (string != null) {
                    longs.add(Long.parseLong(string));
                }
            }
        }
        return longs;
    }

    protected boolean isUserMemberOfGroup(User user, Group group) {
        // unit admins / specialty admins can only add groups they belong to
        if (ApiUtil.userHasRole(user, RoleName.GLOBAL_ADMIN)) {
            return true;
        }

        if (ApiUtil.userHasRole(user, RoleName.SPECIALTY_ADMIN)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getRole().getRoleType().getValue().equals(RoleType.STAFF)) {

                    // check if have direct membership of group
                    if (groupRole.getGroup().equals(group)) {
                        return true;
                    }

                    // check if group is one of the child groups of user's specialty
                    for (GroupRelationship groupRelationship : group.getGroupRelationships()) {
                        if (groupRelationship.getRelationshipType().equals(RelationshipTypes.PARENT)
                                && groupRelationship.getSourceGroup().equals(group)) {
                            return true;
                        }
                    }
                }
            }
        }

        if (ApiUtil.userHasRole(user, RoleName.UNIT_ADMIN)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                // check if have direct membership of group
                if (groupRole.getRole().getName().equals(RoleName.UNIT_ADMIN) && groupRole.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        if (ApiUtil.userHasRole(user, RoleName.GP_ADMIN)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                // check if have direct membership of group
                if (groupRole.getRole().getName().equals(RoleName.GP_ADMIN) && groupRole.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        if (ApiUtil.userHasRole(user, RoleName.STAFF_ADMIN)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getRole().getName().equals(RoleName.STAFF_ADMIN) && groupRole.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        if (ApiUtil.userHasRole(user, RoleName.DISEASE_GROUP_ADMIN)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getRole().getName().equals(RoleName.DISEASE_GROUP_ADMIN)
                        && groupRole.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        if (ApiUtil.userHasRole(user, RoleName.PATIENT)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getRole().getName().equals(RoleName.PATIENT) && groupRole.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        return false;
    }
}
