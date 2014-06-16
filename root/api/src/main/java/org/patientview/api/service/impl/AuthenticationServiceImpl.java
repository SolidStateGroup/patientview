package org.patientview.api.service.impl;

import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;

/**
 *
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@Service
public class AuthenticationServiceImpl implements org.patientview.api.service.AuthenticationService {


    @Inject
    private UserRepository userRepository;

    @Inject
    private UserTokenRepository userTokenRepository;

    public UserToken authenticate(String username, String password) throws UsernameNotFoundException {
        User user  = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("The username provided as not been found");
        }

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(CommonUtils.getAuthtoken());
        userToken.setCreated(new Date());

       // userToken = userTokenRepository.save(userToken);

        return userToken;

    }

    public UserToken getToken(String token) {
        return userTokenRepository.findByToken(token);
    }

    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }
}
