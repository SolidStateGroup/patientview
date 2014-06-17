package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.AuthenticationServiceImpl;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTokenRepository userTokenRepository;

    @InjectMocks
    private AuthenticationService authenticationService = new AuthenticationServiceImpl();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAuthenticate() {
        String username = "testUsername";
        String password = "doNotShow";
        try {
            authenticationService.authenticate(username, password);
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }
        when(userRepository.findByUsername(any(String.class))).thenReturn(new User());
    }
}
