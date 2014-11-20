package org.patientview.api.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.AuthenticationServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private UserService userService;

    @Mock
    private UserTokenRepository userTokenRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private Properties properties;

    @InjectMocks
    private AuthenticationService authenticationService = new AuthenticationServiceImpl();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // mock request, used when authenticating and getting request remote IP address
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        when(properties.getProperty(eq("maximum.failed.logons"))).thenReturn("3");
        when(properties.getProperty(eq("session.length"))).thenReturn("1800000");
        this.authenticationService.setParameter();
    }

    @After
    public void cleanUp() {
        RequestContextHolder.resetRequestAttributes();
    }

    /**
     * Test: Test that the authentication service and handled the password match.
     * Fail: The method cannot validation the user.
     */
    @Test
    public void testAuthenticate() {

        String password = "doNotShow";

        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(true);
        user.setLocked(Boolean.FALSE);

        UserToken userToken = new UserToken();
        userToken.setUser(user);

        try {
            when(userRepository.findByUsername(any(String.class))).thenReturn(user);
            when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);
            authenticationService.authenticate(user.getUsername(), password);
        } catch (Exception e) {
            Assert.fail("This call should not fail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Test: Create a authentication from a token with a positive outcome
     * Fail: The authentication has not been done
     */
    @Test
    public void testAuthenticatePreAuthenticationToken() {
        String testToken = "XXX-XXX-XXX";

        User tokenUser = TestUtils.createUser("TokenUser");

        UserToken userToken = new UserToken();
        userToken.setUser(tokenUser);
        userToken.setToken(testToken);
        userToken.setExpiration(new Date());
        userToken.setCreated(new Date());

        Authentication authenticationToken = new PreAuthenticatedAuthenticationToken(testToken, testToken);

        when(userTokenRepository.findByToken(eq(testToken))).thenReturn(userToken);
        when(roleRepository.findByUser(tokenUser)).thenReturn(Collections.EMPTY_LIST);
        Authentication authentication = null;
        try {
            authentication = authenticationService.authenticate(authenticationToken);
        } catch (AuthenticationServiceException a) {
            Assert.fail("An exception should not have been raised");
        }

        Assert.assertTrue("The authentication objects should now be authenticated", authentication.isAuthenticated());
        Assert.assertNotNull("The principal should not be null", authentication.getPrincipal());
    }

    /**
     * Test: Create a authentication form a token with a negative outcome
     * Fail: The authentication has not been done
     */
    @Test(expected = AuthenticationServiceException.class)
    public void testAuthenticatePreAuthenticationToken_Failure() throws AuthenticationServiceException{
        String testToken = "XXX-XXX-ZZZ";

        User tokenUser = TestUtils.createUser("TokenUser");

        Authentication authenticationToken = new PreAuthenticatedAuthenticationToken(testToken, testToken);

        when(userTokenRepository.findByToken(eq(testToken))).thenReturn(null);
        when(roleRepository.findByUser(tokenUser)).thenReturn(Collections.EMPTY_LIST);

        authenticationService.authenticate(authenticationToken);
        Assert.fail("An service exception should  been raised");
    }

    /**
     * Test: Create a authentication from a token with a positive outcome with granted authorities associated to it
     * Fail: The authentication has not been done and no authorities returned
     */
    @Test
    public void testAuthenticatePreAuthenticationToken_Authorities() {
        String testToken = "XXX-XXX-KKK";

        User tokenUser = TestUtils.createUser("TokenUser");

        UserToken userToken = new UserToken();
        userToken.setUser(tokenUser);
        userToken.setToken(testToken);
        userToken.setExpiration(new Date());
        userToken.setCreated(new Date());

        Role authority = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        List<Role> authorities = new ArrayList<Role>();
        authorities.add(authority);

        Authentication authenticationToken = new PreAuthenticatedAuthenticationToken(testToken, testToken);

        when(userTokenRepository.findByToken(eq(testToken))).thenReturn(userToken);
        when(roleRepository.findByUser(tokenUser)).thenReturn(authorities);

        Authentication authentication = null;
        try {
            authentication = authenticationService.authenticate(authenticationToken);
        } catch (AuthenticationServiceException a) {
            Assert.fail("An exception should not have been raised");
        }

        Assert.assertTrue("The authentication objects should now be authenticated", authentication.isAuthenticated());
        Assert.assertNotNull("The principal should not be null", authentication.getPrincipal());
        Assert.assertNotNull("The authorities should not be null", authentication.getAuthorities());
    }

    /**
     * Test: Try and authenticate against an account that's locked
     * Fail: An exception is not raised
     */
    @Test(expected = AuthenticationServiceException.class)
    public void testLockAccount() throws AuthenticationServiceException {
        String password = "doNotShow";

        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(Boolean.TRUE);
        user.setLocked(Boolean.TRUE);

        when(userRepository.findByUsername(any(String.class))).thenReturn(user);
        authenticationService.authenticate(user.getUsername(), password);
    }

    /**
     * Test: Try and authenticate against when 2 attempts have already been made
     * Fail: An exception is not raised
     */
    @Test(expected = AuthenticationServiceException.class)
    public void testAfterThreeLoginAttempt() throws AuthenticationServiceException {
        String password = "doNotShow";

        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(Boolean.TRUE);
        user.setLocked(Boolean.FALSE);
        user.setFailedLogonAttempts(3);

        when(userRepository.findByUsername(any(String.class))).thenReturn(user);
        authenticationService.authenticate(user.getUsername(), "NotThePasswordWanted");
    }

    @Test
    public void testSwitchUser() throws AuthenticationServiceException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to switch to
        User switchUser = TestUtils.createUser("switch");
        Role switchRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(switchRole, group, switchUser);
        Set<GroupRole> groupRolesSwitch = new HashSet<>();
        groupRolesSwitch.add(groupRoleStaff);
        switchUser.setGroupRoles(groupRolesSwitch);

        Date now = new Date();
        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(CommonUtils.getAuthToken());
        userToken.setCreated(now);
        userToken.setExpiration(new Date(now.getTime()));

        when(userRepository.findOne(eq(switchUser.getId()))).thenReturn(switchUser);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);
        when(userService.currentUserCanSwitchToUser(eq(switchUser))).thenReturn(true);

        authenticationService.switchToUser(switchUser.getId());
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testSwitchUser_CurrentlyPatient() throws AuthenticationServiceException {

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // user to switch to
        User switchUser = TestUtils.createUser("switch");
        Role switchRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRoleStaff = TestUtils.createGroupRole(switchRole, group, switchUser);
        Set<GroupRole> groupRolesSwitch = new HashSet<>();
        groupRolesSwitch.add(groupRoleStaff);
        switchUser.setGroupRoles(groupRolesSwitch);

        Date now = new Date();
        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(CommonUtils.getAuthToken());
        userToken.setCreated(now);
        userToken.setExpiration(new Date(now.getTime()));

        when(userRepository.findOne(eq(switchUser.getId()))).thenReturn(switchUser);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);
        when(userService.currentUserCanSwitchToUser(eq(switchUser))).thenReturn(true);

        authenticationService.switchToUser(switchUser.getId());
    }
}
