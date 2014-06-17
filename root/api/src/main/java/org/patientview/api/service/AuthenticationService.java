package org.patientview.api.service;

import org.apache.http.auth.AuthenticationException;
import org.patientview.persistence.model.UserToken;
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

    UserToken authenticate(String username, String password) throws AuthenticationException, UsernameNotFoundException;

    UserToken getToken(String token);
}
