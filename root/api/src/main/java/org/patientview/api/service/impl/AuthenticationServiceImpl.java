package org.patientview.api.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.patientview.api.service.AuthenticationService;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Service
public class AuthenticationServiceImpl extends AbstractServiceImpl<AuthenticationServiceImpl> implements AuthenticationService {

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserTokenRepository userTokenRepository;

    @Inject
    private RoleRepository roleRepository;

    public UserToken authenticate(String username, String password) throws UsernameNotFoundException,
            AuthenticationServiceException {

        LOG.debug("Trying to authenticate user: {}", username);

        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("The username provided has not been found");
        }

        if (!user.getPassword().equals(DigestUtils.sha256Hex(password))) {
            throw new AuthenticationServiceException("Invalid credentials");
        }

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(CommonUtils.getAuthtoken());
        userToken.setCreated(new Date());
        userToken = userTokenRepository.save(userToken);

        user.setLastLogin(new Date());
        userRepository.save(user);

        return userToken;

    }

    public Authentication authenticate(final Authentication authentication) throws AuthenticationServiceException {
        UserToken userToken = userTokenRepository.findByToken(authentication.getName());

        if (userToken != null) {

            List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

            for (Role role : roleRepository.findByUser(userToken.getUser())) {
                grantedAuthorities.add((GrantedAuthority) role);
            }
            return new UsernamePasswordAuthenticationToken(userToken.getUser(), userToken.getUser().getId(),
                    grantedAuthorities);

        } else {
            throw new AuthenticationServiceException("Token could not be found");
        }


    }

    public UserToken getToken(String token) {
        return userTokenRepository.findByToken(token);
    }

    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    public void logout(String token) throws AuthenticationServiceException {
        UserToken userToken = userTokenRepository.findByToken(token);

        if (userToken == null) {
            throw new AuthenticationServiceException("User is not currently logged in");
        }

        userTokenRepository.delete(userToken.getId());
    }
}
