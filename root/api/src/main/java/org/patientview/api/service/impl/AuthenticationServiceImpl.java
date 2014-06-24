package org.patientview.api.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.auth.AuthenticationException;
import org.patientview.api.service.AuthenticationService;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Date;

/**
 *
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserTokenRepository userTokenRepository;

    @PostConstruct
    private void init() {
        LOG.info("Authentication service started");
    }

    public UserToken authenticate(String username, String password) throws UsernameNotFoundException,
            AuthenticationException {

        LOG.debug("Trying to authenticate user: {}", username);

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("The username provided has not been found");
        }

        if (!user.getPassword().equals(DigestUtils.sha256Hex(password))) {
            throw new AuthenticationException("Invalid credentials");
        }

        if (!user.getVerified()) {
            throw new AuthenticationException("User has not been verified");
        }

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(CommonUtils.getAuthtoken());
        userToken.setCreated(new Date());

        userToken = userTokenRepository.save(userToken);

        return userToken;

    }

    public UserToken getToken(String token) {
        return userTokenRepository.findByToken(token);
    }

    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    public void logout(String token) throws AuthenticationException {
        UserToken userToken = userTokenRepository.findByToken(token);

        if (userToken == null) {
            throw new AuthenticationException("User is not currently logged in");
        }

        userTokenRepository.delete(userToken.getId());
    }
}
