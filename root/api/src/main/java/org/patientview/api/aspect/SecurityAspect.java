package org.patientview.api.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.patientview.api.service.GroupService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 25/07/2014
 *
 * http://stackoverflow.com/questions/3271659/use-enum-type-as-a-value-parameter-for-rolesallowed-annotation
 *
 */

@Aspect
@Component
public class SecurityAspect {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAspect.class);

    @Inject
    private GroupService groupService;

    public SecurityAspect() {
        LOG.info("Security Aspect Initialised");
    }



    @Before("@annotation(org.patientview.api.annotation.GroupMemberOnly)")
    public void checkGroupMembership(final JoinPoint joinPoint) throws Throwable {


        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user == null) {
            throw new SecurityException("The user must be authenticated");
        }

        List<Group> groups = groupService.findGroupByUser(user);

        Roles[] roles = Util.getRoles(joinPoint);

    }



}
