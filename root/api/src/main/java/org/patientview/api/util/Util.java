package org.patientview.api.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Static utility methods.
 *
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public final class Util {

    private Util() { }

    /**
     * Convert the Iterable<T> type passed for Spring DAO interface into a more useful typed List.
     * @param iterable Iterable to convert to typed List
     * @param <T> Type of List to create
     * @return Typed List
     */
    public static <T> List<T> convertIterable(Iterable<T> iterable) {
        if (iterable == null) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<T>();
        Iterator<T> lookupIterator = iterable.iterator();

        while (lookupIterator.hasNext()) {
            list.add(lookupIterator.next());
        }
        return list;
    }

    /**
     * Check a String appears in an Enum.
     * @param value String value to find in Enum
     * @param enumClass Class of Enum to search
     * @param <E> Type of Enum
     * @return True if String exists in Enum, false if not
     */
    public static <E extends Enum<E>> boolean isInEnum(String value, Class<E> enumClass) {
        for (E e : enumClass.getEnumConstants()) {
            if (e.name().equals(value)) { return true; }
        }
        return false;
    }

    /**
     * Retrieve a an Array of RoleName from an aspect JoinPoint.
     * @param joinPoint Aspect JoinPoint
     * @return Array of RoleName
     */
    public static RoleName[] getRoles(JoinPoint joinPoint) {
        final org.aspectj.lang.Signature signature = joinPoint.getStaticPart().getSignature();
        if (signature instanceof MethodSignature) {
            final MethodSignature ms = (MethodSignature) signature;

            List<Class> classTypes = new ArrayList<>();
            classTypes.add(GroupMemberOnly.class);
            classTypes.add(RoleOnly.class);

            for (Annotation annotation : ms.getMethod().getDeclaredAnnotations()) {
                if (classTypes.contains(annotation.annotationType())) {
                    return Util.getRolesFromAnnotation(annotation);
                }
            }
        }
        return null;
    }

    /**
     * Retrieve a an Array of RoleName from an aspect Annotation.
     * @param annotation Aspect Annotation
     * @return Array of RoleName
     */
    private static RoleName[] getRolesFromAnnotation(Annotation annotation) {
        Method[] methods = annotation.annotationType().getMethods();
        for (Method method : methods) {
            String name = method.getName();
            Class<?> returnType = method.getReturnType();
            Class<?> componentType = returnType.getComponentType();
            if (name.equals("roles") && returnType.isArray()
                    && RoleName.class.isAssignableFrom(componentType)) {
                RoleName[] features;
                try {
                    features = (RoleName[]) (method.invoke(annotation, new Object[] {}));
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Error executing value() method in annotation.getClass().getCanonicalName()", e);
                }
                return features;
            }
        }
        throw new RuntimeException("No value() method returning a Roles[] roles was found in annotation "
                        + annotation.getClass().getCanonicalName());
    }

    /**
     * Convert a collection of GrantedAuthority to a typed List.
     * @param grantedAuthorities Collection of GrantedAuthority
     * @param <T> Type to convert to
     * @return List of converted GrantedAuthority
     */
    public static <T> List<T> convertAuthorities(Collection<? extends GrantedAuthority>  grantedAuthorities) {
        List<T> list = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            list.add((T) grantedAuthority);
        }
        return list;
    }

    /**
     * Check if current User is a member of a Group with a certain Role.
     * @param groupId ID of Group to find Role membership
     * @param roleNames One or more RoleName to find membership of
     * @return True if User is a member, false if not
     */
    public static boolean doesContainGroupAndRole(Long groupId, RoleName... roleNames) {
        if (CollectionUtils.isEmpty(getGroupRoles())) {
            return false;
        }

        for (GroupRole groupRole : getGroupRoles()) {
            if (groupRole.getGroup().getId().equals(groupId)) {
                for (RoleName roleNameArg : roleNames) {
                    if (groupRole.getRole().getName().equals(roleNameArg)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Check if current User is a member of a Group with a certain Role, including child groups (for specialty admins).
     * @param groupId ID of Group to find Role membership
     * @param roleName RoleName to find membership of
     * @return True if User is a member, false if not
     */
    public static boolean doesContainChildGroupAndRole(Long groupId, RoleName roleName) {
        if (CollectionUtils.isEmpty(getGroupRoles())) {
            return false;
        }

        for (GroupRole groupRole : getGroupRoles()) {
            Group group = groupRole.getGroup();
            Role role = groupRole.getRole();

            if (role.getName().equals(roleName)) {
                for (Group childGroup : group.getChildGroups()) {
                    if (childGroup.getId().equals(groupId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if current User is a member of a Group with a certain Role, including parent groups (for specialty admins).
     * Note: not currently used.
     * @param groupId ID of Group to find Role membership
     * @param roleName RoleName to find membership of
     * @return True if User is a member, false if not
     */
    public static boolean doesContainParentGroupAndRole(Long groupId, RoleName roleName) {
        if (CollectionUtils.isEmpty(getGroupRoles())) {
            return false;
        }

        for (GroupRole groupRole : getGroupRoles()) {
            Group group = groupRole.getGroup();
            Role role = groupRole.getRole();

            if (role.getName().equals(roleName)) {
                for (Group parentGroup : group.getParentGroups()) {
                    if (parentGroup.getId().equals(groupId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if current User has one or more Roles.
     * @param roleNames One or more RoleName to find membership of
     * @return True if User is a member, false if not
     * @throws SecurityException
     */
    public static boolean doesContainRoles(RoleName... roleNames) throws SecurityException {
        if (CollectionUtils.isEmpty(getGroupRoles())) {
            return false;
        }
        for (GroupRole groupRole : getGroupRoles()) {
            for (RoleName roleNameArg : roleNames) {
                if (groupRole.getRole().getName().equals(roleNameArg)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get GroupRoles from current SecurityContext, originally stored after login.
     * @return List of GroupRole
     */
    public static List<GroupRole> getGroupRoles() {
        if (SecurityContextHolder.getContext() == null
                || SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new SecurityException("Session is not authenticated");
        }
        return convertAuthorities(SecurityContextHolder.getContext().getAuthentication().getAuthorities());
    }

    /**
     * Get the User object associated with the current security context Principal.
     * @return User object
     */
    public static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Session is not authenticated");
        }
        return (User) authentication.getPrincipal();
    }


    /**
     * Check that current User is an API user (UNIT_ADMIN_API) for a Group that the User belongs to, used when
     * retrieving Observations for a User (can only be done by a User for themselves or by a UNIT_ADMIN_API User).
     * @param user User to check (not current User)
     * @return True if current User is an API user for a Group that the User belongs to
     */
    public static boolean isCurrentUserApiUserForUser(User user) {
        for (GroupRole userGroupRole : user.getGroupRoles()) {
            Group userGroup = userGroupRole.getGroup();
            Role userRole = userGroupRole.getRole();
            if (userRole.getName().equals(RoleName.GLOBAL_ADMIN)) {
                return true;
            }

            for (GroupRole currentUserGroupRole : getGroupRoles()) {
                if (currentUserGroupRole.getRole().getName().equals(RoleName.GLOBAL_ADMIN)) {
                    return true;
                }
                if (currentUserGroupRole.getRole().getName().equals(RoleName.UNIT_ADMIN_API)
                        && currentUserGroupRole.getGroup().equals(userGroup)) {
                    return true;
                }
            }
        }

        return false;
    }
    /**
     * Create a FHIR ResourceReference from a UUID.
     * @param uuid UUID to convert to FHIR ResourceReference
     * @return ResourceReference object
     */
    public static ResourceReference createFhirResourceReference(UUID uuid) {
        ResourceReference resourceReference = new ResourceReference();
        resourceReference.setDisplaySimple(uuid.toString());
        resourceReference.setReferenceSimple("uuid");
        return resourceReference;
    }

    /**
     * Get the time difference in TimeUnit between two dates, used for timing migration.
     * @param date1 Start time
     * @param date2 End time
     * @param timeUnit TimeUnit e.g. TimeUnit.SECONDS
     * @return long representation of time difference
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}

