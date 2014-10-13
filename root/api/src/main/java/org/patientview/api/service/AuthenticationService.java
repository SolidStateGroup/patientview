package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.UserToken;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AuthenticationService extends UserDetailsService {

    void setParameter();

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN })
    String switchUser(Long userId, String token) throws AuthenticationServiceException;

    String authenticate(String username, String password)
            throws AuthenticationServiceException, UsernameNotFoundException;

    Authentication authenticate(final Authentication authentication) throws AuthenticationServiceException;

    void logout(String token) throws AuthenticationServiceException;

    UserToken getUserInformation(String token);
}
