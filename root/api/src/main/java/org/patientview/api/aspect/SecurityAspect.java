package org.patientview.api.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.patientview.api.exception.ResourceForbiddenException;
import org.patientview.api.service.GroupService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 25/07/2014
 *
 * http://stackoverflow.com/questions/3271659/use-enum-type-as-a-value-parameter-for-rolesallowed-annotation
 *
 * Responsible for security resource via annotations.
 *
 */

@Aspect
@Configurable
public class SecurityAspect {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAspect.class);

    private static SecurityAspect instance;

    @Inject
    private GroupService groupService;

    @Inject
    private GroupRepository groupRepository;

    private SecurityAspect() {
        LOG.info("Security Aspect Initialised");
    }

    @PostConstruct
    public void init() {
        if (groupService == null || groupRepository == null) {
            throw new IllegalStateException("Injection failed for aspect");
        } else {
            LOG.info("Security aspect started correctly");
        }
    }

    @Before("@annotation(org.patientview.api.annotation.GroupMemberOnly)")
    public void checkGroupMembership(JoinPoint joinPoint) throws ResourceForbiddenException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Cannot validate when security has not been initialised
        if (authentication == null || !authentication.isAuthenticated() ) {
            throw new SecurityException("The request must be authenticated");
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Cannot validate without the principal
        if (user == null) {
            throw new SecurityException("The user must be authenticated");
        }

        // Refactor later into two different PointCuts - one for groupIds being passed, another for whole groups
        Long groupId = getGroupId(joinPoint);
        Group group;
        if (groupId == null) {
            group = getGroup(joinPoint);
        } else {
            group = groupRepository.findOne(groupId);
        }

        if (group == null) {
            LOG.error("Cannot validate against a group that does not exist");
            return;
        }

        RoleName[] roles = Util.getRoles(joinPoint);

        if (doesUserHavePermissions(roles)) {
            LOG.debug("User has passed group validation");
        } else {
            throw new ResourceForbiddenException("The user does not belong to this group");
        }


        LOG.info("PointCut");
    }

    private boolean doesUserHavePermissions(RoleName[] roles) {
        for (Group group : Util.convertIterable(groupService.findGroupByUser(Util.getUser()))) {
            if (Util.doesContainGroupAndRole(group, roles)) {
                return true;
            }
        }
        return false;
    }

    // TODO the next two methods can be fixed up with annotations on the parameters
    // Assuming we apply the annotation to a method with a groupId
    private Long getGroupId(JoinPoint joinPoint) {

        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof Long) {
                return (Long) argument;
            }
        }

        return null;

    }

    // Assuming we apply the annotation to a method with a Group
    private Group getGroup(JoinPoint joinPoint) {

        for (Object argument : joinPoint.getArgs()) {
            if (argument instanceof Group) {
                return (Group) argument;
            }
        }

        return null;
    }


    public static SecurityAspect aspectOf(){

        if (instance == null) {
            instance = new SecurityAspect();
        }
        return instance;

    }

}
