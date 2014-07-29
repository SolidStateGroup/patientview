package org.patientview.api.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.patientview.api.service.GroupService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collection;
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


    @Pointcut("@annotation(org.patientview.api.annotation.GroupMemberOnly)")
    public void securityGroupAnnotation() {}



    @Before("securityGroupAnnotation()")
    public void checkGroupMembership(JoinPoint joinPoint) {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            throw new SecurityException("The user must be authenticated");
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user == null) {
            throw new SecurityException("The user must be authenticated");
        }



        Roles[] roles = Util.getRoles(joinPoint);

        // TODO - retrieve group id from method call
        if (doesUserHaveRoles(roles, SecurityContextHolder.getContext().getAuthentication().getAuthorities())) {
            List<Group> groups = groupService.findGroupByUser(user);
            if (groups.contains(null)) {

            }

        }


        LOG.info("PointCut");
    }

    private boolean doesUserHaveRoles(Roles[] annotatedRoles,
                                      Collection<? extends GrantedAuthority> grantedAuthorities) {
        for (Roles roles : annotatedRoles) {
            for (GrantedAuthority grantedAuthority : grantedAuthorities) {
                Role role = (Role) grantedAuthority;
                if (role.getName().equals(roles)) {
                    return true;
                }
            }
        }

        return false;
    }




}
