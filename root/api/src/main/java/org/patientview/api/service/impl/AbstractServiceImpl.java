package org.patientview.api.service.impl;

import org.apache.commons.lang.ArrayUtils;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RelationshipTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
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

    protected List<Role> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Session is not authenticated");
        }

        if (CollectionUtils.isEmpty(authentication.getAuthorities())) {
            return Collections.EMPTY_LIST;
        }

        return Util.convertAuthorities(authentication.getAuthorities());
    }

    protected User getUser() {
        return Util.getUser();
    }

    protected boolean doesContainRoles(RoleName... roleNames) {
        return Util.currentUserHasRole(roleNames);
    }

    protected static <T> List<T> convertIterable(Iterable<T> iterable) {
        return Util.convertIterable(iterable);
    }


    @PostConstruct
    public void init() {
        LOG.info("Service started");
    }

    @PreDestroy
    public void close() {
        LOG.info("Service closing");
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
        if (Util.userHasRole(user, RoleName.GLOBAL_ADMIN)) {
            return true;
        }

        if (Util.userHasRole(user, RoleName.SPECIALTY_ADMIN)) {
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

        if (Util.userHasRole(user, RoleName.UNIT_ADMIN)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                // check if have direct membership of group
                if (groupRole.getRole().getName().equals(RoleName.UNIT_ADMIN) && groupRole.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        if (Util.userHasRole(user, RoleName.STAFF_ADMIN)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getRole().getName().equals(RoleName.STAFF_ADMIN) && groupRole.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        if (Util.userHasRole(user, RoleName.DISEASE_GROUP_ADMIN)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getRole().getName().equals(RoleName.DISEASE_GROUP_ADMIN)
                        && groupRole.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        if (Util.userHasRole(user, RoleName.PATIENT)) {
            for (GroupRole groupRole : user.getGroupRoles()) {
                if (groupRole.getRole().getName().equals(RoleName.PATIENT) && groupRole.getGroup().equals(group)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
