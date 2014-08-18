package org.patientview.api.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.controller.model.GroupStatisticTO;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.StatisticType;
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
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public class Util {

    private Util() {}

    /**
     * This is convert the Iterable<T> type passed for Spring DAO interface into
     * a more useful typed List.
     *
     * @param iterable
     * @param <T>
     * @return
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

    // Retrieve the list of Roles from the annotation.
    public static RoleName[] getRoles(JoinPoint joinPoint) {
        final org.aspectj.lang.Signature signature = joinPoint.getStaticPart().getSignature();
        if (signature instanceof MethodSignature) {
            final MethodSignature ms = (MethodSignature) signature;

            for (Annotation annotation : ms.getMethod().getDeclaredAnnotations())
                if (annotation.annotationType() == GroupMemberOnly.class) {
                    return Util.getRolesFromAnnotation(annotation);
                }
        }
        return null;
    }

    public static RoleName[] getRolesFromAnnotation(Annotation annotation) {
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

    // TODO sprint 3 included in the ENUM fix
    public static Collection<GroupStatisticTO> convertGroupStatistics(final List<GroupStatistic> groupStatistics) {

        Map<Date, GroupStatisticTO> groupStatisticTOs = new TreeMap<>();

        for (GroupStatistic groupStatistic : groupStatistics) {
            StatisticType statisticType = StatisticType.valueOf(groupStatistic.getStatisticType().getValue());
            GroupStatisticTO groupStatisticTO = getGroupStatisticTO(groupStatisticTOs, groupStatistic.getStartDate());
            groupStatisticTO.setStartDate(groupStatistic.getStartDate());
            groupStatisticTO.setEndDate(groupStatistic.getEndDate());
            switch (statisticType) {
                case ADD_PATIENT_COUNT:
                    groupStatisticTO.setCountOfPatientAdds(groupStatistic.getValue());
                    break;
                case DELETE_PATIENT_COUNT:
                    groupStatisticTO.setCountOfPatientDeletes(groupStatistic.getValue());
                    break;
                case IMPORT_COUNT:
                    groupStatisticTO.setCountOfImportLoads(groupStatistic.getValue());
                    break;
                case IMPORT_FAIL_COUNT:
                    groupStatisticTO.setCountOfImportFails(groupStatistic.getValue());
                    break;
                case LOGON_COUNT:
                    groupStatisticTO.setCountOfLogons(groupStatistic.getValue());
                    break;
                case UNIQUE_LOGON_COUNT:
                    groupStatisticTO.setCountOfUniqueLogons(groupStatistic.getValue());
                    break;
                case PASSWORD_CHANGE_COUNT:
                    groupStatisticTO.setCountOfPasswordChanges(groupStatistic.getValue());
                    break;
                case ACCOUNT_LOCKED_COUNT:
                    groupStatisticTO.setCountOfAccountLocks(groupStatistic.getValue());
                    break;
                case VIEW_PATIENT_COUNT:
                    groupStatisticTO.setCountOfPatientViews(groupStatistic.getValue());
                    break;
                case PATIENT_COUNT:
                    groupStatisticTO.setCountOfPatients(groupStatistic.getValue());
                    break;
                case REMOVE_PATIENT_COUNT:
                    groupStatisticTO.setCountOfPatientRemoves(groupStatistic.getValue());
                    break;
            }
        }
        return groupStatisticTOs.values();
    }

    private static GroupStatisticTO getGroupStatisticTO(Map<Date, GroupStatisticTO> groupStatisticTOMap, Date startDate)  {
        if (groupStatisticTOMap.get(startDate) == null) {
            groupStatisticTOMap.put(startDate, new GroupStatisticTO());
        }
        return groupStatisticTOMap.get(startDate);
    }

    public static <T> List<T> convertAuthorities(Collection<? extends GrantedAuthority>  grantedAuthorities) {
        List<T> list = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : grantedAuthorities) {
            list.add((T) grantedAuthority);
        }
        return list;
    }

    public static boolean doesContainGroupAndRole(Group group, RoleName... roleNames) {
        if (CollectionUtils.isEmpty(getGroupRoles())) {
            return false;
        }

        for (GroupRole groupRole : getGroupRoles()) {
            if (groupRole.getGroup().equals(group)) {
                for (RoleName roleNameArg : roleNames) {
                    if (groupRole.getRole().getName().equals(roleNameArg)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

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

    public static List<GroupRole> getGroupRoles() {
        if (SecurityContextHolder.getContext() == null
                || SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new SecurityException("Session is not authenticated");
        }
        List<GroupRole> groupRoles = convertAuthorities(
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        return groupRoles;
    }

    public static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Session is not authenticated");
        }

        return (User) authentication.getPrincipal();
    }
}

