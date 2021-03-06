package org.patientview.api.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.Credentials;
import org.patientview.api.service.impl.AuthenticationServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.ApiKey;
import org.patientview.persistence.model.ExternalStandard;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserToken;
import org.patientview.persistence.model.enums.ApiKeyTypes;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.model.enums.UserTokenTypes;
import org.patientview.persistence.repository.ApiKeyRepository;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.ExternalStandardRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.repository.UserTokenRepository;
import org.patientview.service.AuditService;
import org.patientview.test.util.TestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
public class AuthenticationServiceTest {

    @Mock
    private ApiConditionService apiConditionService;

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Mock
    private AuditRepository auditRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ExternalStandardRepository externalStandardRepository;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private Properties properties;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private SecurityService securityService;

    @Mock
    private StaticDataManager staticDataManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserTokenRepository userTokenRepository;

    @InjectMocks
    private AuthenticationService authenticationService = new AuthenticationServiceImpl();

    ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // mock request, used when authenticating and getting request remote IP address
        MockHttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        when(properties.getProperty(eq("maximum.failed.logons"))).thenReturn("3");
        when(properties.getProperty(eq("session.length"))).thenReturn("1800000");
        when(properties.getProperty(eq("session.length.mobile"))).thenReturn("631139040000"); // 20 years
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
        String token = "abc123456";

        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(true);
        user.setLocked(false);
        user.setDeleted(false);

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(token);

        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);

        org.patientview.api.model.UserToken returned
                = authenticationService.authenticate(new Credentials(user.getUsername(), password));

        Assert.assertNotNull("token should be set", returned.getToken());
        Assert.assertEquals("correct token should be set", userToken.getToken(), returned.getToken());

        verify(userTokenRepository, times(1)).deleteExpiredByUserId(eq(user.getId()));
        verify(auditService, times(1)).createAudit(eq(AuditActions.LOGGED_ON), eq(user.getUsername()),
                eq(user), eq(user.getId()), eq(AuditObjectTypes.User), any(Group.class));
    }

    /**
     * Test authentication with an apiKey
     */
    @Test
    public void testAuthenticate_apiKey() {
        String password = "doNotShow";
        String salt = "saltsaltsalt";

        // User
        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password + salt));
        user.setEmailVerified(true);
        user.setSalt(salt);
        user.setLocked(false);
        user.setDeleted(false);
        user.setSalt(salt);
        user.setSecretWord("{"
                + "\"salt\" : \"" + salt + "\", "
                + "\"1\" : \"" + DigestUtils.sha256Hex("A" + salt) + "\", "
                + "\"2\" : \"" + DigestUtils.sha256Hex("B" + salt) + "\", "
                + "\"3\" : \"" + DigestUtils.sha256Hex("C" + salt) + "\", "
                + "\"4\" : \"" + DigestUtils.sha256Hex("D" + salt) + "\" "
                + "}");

        // UserToken
        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken("sometoken");

        // ApiKey
        ApiKey apiKey = new ApiKey();
        apiKey.setType(ApiKeyTypes.CKD);
        apiKey.setExpiryDate(new DateTime(new Date()).plusMonths(1).toDate());
        apiKey.setKey("abc123");

        when(apiKeyRepository.findOneByKey(eq(apiKey.getKey()))).thenReturn(apiKey);
        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);

        org.patientview.api.model.UserToken returned
                = authenticationService.authenticate(new Credentials(user.getUsername(), password, apiKey.getKey()));

        Assert.assertNotNull("token should be set", returned.getToken());
        Assert.assertEquals("correct token should be set", userToken.getToken(), returned.getToken());

        verify(apiKeyRepository, times(1)).findOneByKey(eq(apiKey.getKey()));
        verify(userTokenRepository, times(1)).deleteExpiredByUserId(eq(user.getId()));
        verify(auditService, times(1)).createAudit(eq(AuditActions.LOGGED_ON), eq(user.getUsername()),
                eq(user), eq(user.getId()), eq(AuditObjectTypes.User), any(Group.class));
    }

    /**
     * Test authentication with an apiKey not found
     */
    @Test(expected = AuthenticationServiceException.class)
    public void testAuthenticate_apiKey_KeyNotFound() {
        String password = "doNotShow";

        // User
        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(true);
        user.setLocked(false);
        user.setDeleted(false);

        // UserToken
        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken("sometoken");

        // ApiKey
        ApiKey apiKey = new ApiKey();
        apiKey.setType(ApiKeyTypes.CKD);
        apiKey.setKey("abc123");

        when(apiKeyRepository.findOneByKey(eq(apiKey.getKey()))).thenReturn(null);
        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);

        org.patientview.api.model.UserToken returned
                = authenticationService.authenticate(new Credentials(user.getUsername(), password, apiKey.getKey()));

        Assert.assertNotNull("token should be set", returned.getToken());
    }

    /**
     * Test authentication with an apiKey invalid type
     */
    @Test(expected = AuthenticationServiceException.class)
    public void testAuthenticate_apiKey_TypeInvalid() {
        String password = "doNotShow";

        // User
        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(true);
        user.setLocked(false);
        user.setDeleted(false);

        // UserToken
        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken("sometoken");

        // ApiKey
        ApiKey apiKey = new ApiKey();
        apiKey.setType(ApiKeyTypes.IMPORTER);
        apiKey.setExpiryDate(new DateTime(new Date()).plusMonths(1).toDate());
        apiKey.setKey("abc123");

        when(apiKeyRepository.findOneByKey(eq(apiKey.getKey()))).thenReturn(apiKey);
        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);

        org.patientview.api.model.UserToken returned
                = authenticationService.authenticate(new Credentials(user.getUsername(), password, apiKey.getKey()));

        Assert.assertNotNull("token should be set", returned.getToken());
    }

    /**
     * Make sure only Patient able to logging
     */
    @Test
    public void testAuthenticate_Mobile() throws NoSuchAlgorithmException {
        String password = "mobile";
        String token = "abc123456";

        //  generate secret word
        String salt = CommonUtils.generateSalt();
        User user = TestUtils.createUser("testUser");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setSecretWord("{"
                + "\"salt\" : \"" + salt + "\", "
                + "\"1\" : \"" + DigestUtils.sha256Hex("A" + salt) + "\", "
                + "\"2\" : \"" + DigestUtils.sha256Hex("B" + salt) + "\", "
                + "\"3\" : \"" + DigestUtils.sha256Hex("C" + salt) + "\", "
                + "\"4\" : \"" + DigestUtils.sha256Hex("E" + salt) + "\", "
                + "\"5\" : \"" + DigestUtils.sha256Hex("F" + salt) + "\", "
                + "\"6\" : \"" + DigestUtils.sha256Hex("G" + salt) + "\" "
                + "}");

        // set group and roles
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);

        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(token);

        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);

        org.patientview.api.model.UserToken returned
                = authenticationService.authenticateMobile(new Credentials(user.getUsername(), password));

        // we only return token
        Assert.assertNotNull("secret word token must not be null", returned.getSecretWordToken());

        verify(userTokenRepository, times(1)).deleteExpiredByUserId(eq(user.getId()));
        verify(auditService, times(0)).createAudit(eq(AuditActions.LOGGED_ON), eq(user.getUsername()),
                eq(user), eq(user.getId()), eq(AuditObjectTypes.User), any(Group.class));
    }

    @Test(expected = AuthenticationServiceException.class)
    public void testAuthenticate_Mobile_invalidRole() {
        String password = "mobile";
        String token = "abc123456";

        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(true);
        user.setLocked(false);
        user.setDeleted(false);

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(token);

        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);

        org.patientview.api.model.UserToken returned
                = authenticationService.authenticateMobile(new Credentials(user.getUsername(), password));

        Assert.assertNotNull("token should be set", returned.getToken());
        Assert.assertNotNull("secret word should be set", returned.getSecretWord());
        Assert.assertEquals("correct token should be set", userToken.getToken(), returned.getToken());

        verify(auditService, times(1)).createAudit(eq(AuditActions.LOGGED_ON), eq(user.getUsername()),
                eq(user), eq(user.getId()), eq(AuditObjectTypes.User), any(Group.class));
    }

    @Test
    public void testAuthenticate_secretWord() {
        String password = "doNotShow";
        String token = "abc123456";
        String salt = "saltsaltsalt";

        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(true);
        user.setLocked(false);
        user.setDeleted(false);
        user.setSecretWord("{"
                + "\"salt\" : \"" + salt + "\", "
                + "\"1\" : \"" + DigestUtils.sha256Hex("A" + salt) + "\", "
                + "\"2\" : \"" + DigestUtils.sha256Hex("B" + salt) + "\", "
                + "\"3\" : \"" + DigestUtils.sha256Hex("C" + salt) + "\", "
                + "\"4\" : \"" + DigestUtils.sha256Hex("D" + salt) + "\" "
                + "}");

        UserToken foundUserToken = new UserToken();
        foundUserToken.setUser(user);
        foundUserToken.setToken(token);
        foundUserToken.setCheckSecretWord(true);

        Group group = TestUtils.createGroup("testGroup");
        group.getGroupFeatures().add(
                TestUtils.createGroupFeature(TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(foundUserToken);
        org.patientview.api.model.UserToken returned
                = authenticationService.authenticate(new Credentials(user.getUsername(), password));

        Assert.assertNotNull("secret word token must not be null", returned.getSecretWordToken());
        Assert.assertNotNull("secret word indexes should be set", returned.getSecretWordIndexes());
        Assert.assertEquals("secret word indexes should contain 2 entries", 2, returned.getSecretWordIndexes().size());

        verify(auditService, times(0)).createAudit(eq(AuditActions.LOGGED_ON), eq(user.getUsername()),
                eq(user), eq(user.getId()), eq(AuditObjectTypes.User), any(Group.class));
    }

    /**
     * Unit test to check make sure secret word indexes are returned in ASC order eg 2, 4, 0r 0,8 ect
     */
    @Test
    public void testAuthenticate_secretWord_indexOrder() {
        String password = "doNotShow";
        String token = "abc123456";
        String salt = "saltsaltsalt";

        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(true);
        user.setLocked(false);
        user.setDeleted(false);
        user.setSecretWord("{"
                + "\"salt\" : \"" + salt + "\", "
                + "\"3\" : \"" + DigestUtils.sha256Hex("C" + salt) + "\", "
                + "\"2\" : \"" + DigestUtils.sha256Hex("B" + salt) + "\", "
                + "\"1\" : \"" + DigestUtils.sha256Hex("A" + salt) + "\", "
                + "\"4\" : \"" + DigestUtils.sha256Hex("D" + salt) + "\" "
                + "}");

        UserToken foundUserToken = new UserToken();
        foundUserToken.setUser(user);
        foundUserToken.setToken(token);
        foundUserToken.setCheckSecretWord(true);

        Group group = TestUtils.createGroup("testGroup");
        group.getGroupFeatures().add(
                TestUtils.createGroupFeature(TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(foundUserToken);
        org.patientview.api.model.UserToken returned
                = authenticationService.authenticate(new Credentials(user.getUsername(), password));

        Assert.assertNotNull("secret word token must not be null", returned.getSecretWordToken());
        Assert.assertNotNull("secret word indexes should be set", returned.getSecretWordIndexes());
        Assert.assertEquals("secret word indexes should contain 2 entries", 2, returned.getSecretWordIndexes().size());
        Assert.assertTrue("Index should be in ASC order", Integer.parseInt(returned.getSecretWordIndexes().get(0))
                < Integer.parseInt(returned.getSecretWordIndexes().get(1)));

        verify(auditService, times(0)).createAudit(eq(AuditActions.LOGGED_ON), eq(user.getUsername()),
                eq(user), eq(user.getId()), eq(AuditObjectTypes.User), any(Group.class));
    }

    // test to validate that the same secret words indexes are returned
    @Test
    public void testAuthenticate_secretWord_ExistingToken() {
        String password = "doNotShow";
        String token = "abc123456";
        String salt = "saltsaltsalt";


        List<String> secretWordIndexes = new ArrayList<>();
        secretWordIndexes.add("3");
        secretWordIndexes.add("6");


        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(true);
        user.setLocked(false);
        user.setDeleted(false);
        user.setSecretWord("{"
                + "\"salt\" : \"" + salt + "\", "
                + "\"3\" : \"" + DigestUtils.sha256Hex("C" + salt) + "\", "
                + "\"2\" : \"" + DigestUtils.sha256Hex("B" + salt) + "\", "
                + "\"1\" : \"" + DigestUtils.sha256Hex("A" + salt) + "\", "
                + "\"4\" : \"" + DigestUtils.sha256Hex("D" + salt) + "\" "
                + "}");

        UserToken foundUserToken = new UserToken();
        foundUserToken.setUser(user);
        foundUserToken.setToken(token);
        foundUserToken.setCheckSecretWord(true);
        foundUserToken.setSecretWordToken(token);
        foundUserToken.setSecretWordIndexes(secretWordIndexes);

        // list to return when searching for user
        List<UserToken> userTokens = new ArrayList<>();
        userTokens.add(foundUserToken);

        Group group = TestUtils.createGroup("testGroup");
        group.getGroupFeatures().add(
                TestUtils.createGroupFeature(TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(foundUserToken);
        when(userTokenRepository.findActiveByUser(any(Long.class))).thenReturn(userTokens);
        org.patientview.api.model.UserToken returned
                = authenticationService.authenticate(new Credentials(user.getUsername(), password));

        Assert.assertNotNull("secret word token must not be null", returned.getSecretWordToken());
        Assert.assertNotNull("secret word indexes should be set", returned.getSecretWordIndexes());
        Assert.assertEquals("secret word indexes should contain 2 entries", 2, returned.getSecretWordIndexes().size());
        Assert.assertEquals("secret word indexes 0 should be 3", "3", returned.getSecretWordIndexes().get(0));
        Assert.assertEquals("secret word indexes 1 should be 6", "6", returned.getSecretWordIndexes().get(1));

        verify(auditService, times(0)).createAudit(eq(AuditActions.LOGGED_ON), eq(user.getUsername()),
                eq(user), eq(user.getId()), eq(AuditObjectTypes.User), any(Group.class));
    }

    /**
     * Test: Test that the authentication service and handled the password match.
     * Fail: The method cannot validation the user.
     */
    @Test(expected = AuthenticationServiceException.class)
    public void testAuthenticate_deleted() {

        String password = "doNotShow";

        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(true);
        user.setLocked(false);
        user.setDeleted(true);

        UserToken userToken = new UserToken();
        userToken.setUser(user);

        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);
        authenticationService.authenticate(new Credentials(user.getUsername(), password));
    }

    @Test
    public void testAuthenticateImporter() throws AuthenticationServiceException {
        String password = "doNotShow";
        String apiKeyValue = "abc123";

        Credentials credentials = new Credentials();
        credentials.setUsername("testUsername");
        credentials.setPassword(password);
        credentials.setApiKey(apiKeyValue);

        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER, RoleType.STAFF);

        User user = new User();
        user.setUsername("testUsername");
        user.setPassword(DigestUtils.sha256Hex(password));
        user.setEmailVerified(true);
        user.setLocked(false);
        user.setDeleted(false);
        user.setGroupRoles(new HashSet<GroupRole>());
        user.getGroupRoles().add(TestUtils.createGroupRole(role, group, user));

        ApiKey apiKey = new ApiKey();
        apiKey.setKey(apiKeyValue);
        apiKey.setUser(user);
        apiKey.setType(ApiKeyTypes.IMPORTER);
        List<ApiKey> apiKeys = new ArrayList<>();
        apiKeys.add(apiKey);

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(UUID.randomUUID().toString());

        when(apiKeyRepository.findByKeyAndTypeAndUser(eq(apiKey.getKey()), eq(apiKey.getType()), eq(user)))
                .thenReturn(apiKeys);
        when(userRepository.findByUsernameCaseInsensitive(eq(credentials.getUsername()))).thenReturn(user);
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);
        org.patientview.api.model.UserToken toReturn = authenticationService.authenticateImporter(credentials);

        verify(auditService, times(1)).createAudit(eq(AuditActions.LOGGED_ON), eq(user.getUsername()),
                eq(user), eq(user.getId()), eq(AuditObjectTypes.User), any(Group.class));
        verify(userRepository, times(1)).save(eq(user));
        Assert.assertNotNull("token should not be null", toReturn.getToken());
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
    public void testAuthenticatePreAuthenticationToken_Failure() throws AuthenticationServiceException {
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
        user.setDeleted(false);

        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        authenticationService.authenticate(new Credentials(user.getUsername(), "NotThePasswordWanted"));
    }

    @Test
    public void testCheckSecretWord()
            throws ResourceNotFoundException, ResourceForbiddenException, NoSuchAlgorithmException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        String salt = CommonUtils.generateSalt();

        // create secret word hashmap and convert to json to store in secret word field, each letter is hashed
        String word = "ABC1234";
        Map<String, String> letters = new HashMap<>();
        letters.put("salt", salt);
        for (int i = 0; i < word.length(); i++) {
            letters.put(String.valueOf(i), DigestUtils.sha256Hex(String.valueOf(word.charAt(i)) + salt));
        }

        user.setSecretWord(new JSONObject(letters).toString());
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        Map<String, String> entered = new HashMap<>();
        entered.put("2", "C");
        entered.put("6", "4");

        authenticationService.checkLettersAgainstSecretWord(user, entered, true);
    }

    @Test
    public void testCheckFullSecretWord()
            throws ResourceNotFoundException, ResourceForbiddenException, NoSuchAlgorithmException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        String salt = CommonUtils.generateSalt();

        // create secret word hashmap and convert to json to store in secret word field, each letter is hashed
        String word = "ABCDE";
        Map<String, String> letters = new HashMap<>();
        letters.put("salt", salt);
        for (int i = 0; i < word.length(); i++) {
            letters.put(String.valueOf(i), DigestUtils.sha256Hex(String.valueOf(word.charAt(i)) + salt));
        }

        user.setSecretWord(new JSONObject(letters).toString());
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        Map<String, String> entered = new HashMap<>();
        entered.put("0", "A");
        entered.put("1", "B");
        entered.put("2", "C");
        entered.put("3", "D");
        entered.put("4", "E");

        authenticationService.checkLettersAgainstSecretWord(user, entered, false);
    }


    @Test(expected = ResourceForbiddenException.class)
    public void testCheckFullSecretWord_NotFullWord()
            throws ResourceNotFoundException, ResourceForbiddenException, NoSuchAlgorithmException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        String salt = CommonUtils.generateSalt();

        // create secret word hashmap and convert to json to store in secret word field, each letter is hashed
        String word = "ABC1234";
        Map<String, String> letters = new HashMap<>();
        letters.put("salt", salt);
        for (int i = 0; i < word.length(); i++) {
            letters.put(String.valueOf(i), DigestUtils.sha256Hex(String.valueOf(word.charAt(i)) + salt));
        }

        user.setSecretWord(new JSONObject(letters).toString());
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        Map<String, String> entered = new HashMap<>();
        entered.put("2", "C");
        entered.put("6", "4");

        authenticationService.checkLettersAgainstSecretWord(user, entered, false);
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testCheckSecretWord_indexTooLong()
            throws ResourceNotFoundException, ResourceForbiddenException, NoSuchAlgorithmException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        String salt = CommonUtils.generateSalt();

        // create secret word hashmap and convert to json to store in secret word field, each letter is hashed
        String word = "ABC1234";
        Map<String, String> letters = new HashMap<>();
        letters.put("salt", salt);
        for (int i = 0; i < word.length(); i++) {
            letters.put(String.valueOf(i), DigestUtils.sha256Hex(String.valueOf(word.charAt(i)) + salt));
        }

        user.setSecretWord(new JSONObject(letters).toString());
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        Map<String, String> entered = new HashMap<>();
        entered.put("1", "B");
        entered.put("3", "1");
        entered.put("4", "2");
        entered.put("5", "3");

        authenticationService.checkLettersAgainstSecretWord(user, entered, true);
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testCheckSecretWord_incorrect()
            throws ResourceNotFoundException, ResourceForbiddenException, NoSuchAlgorithmException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        String salt = CommonUtils.generateSalt();

        // create secret word hashmap and convert to json to store in secret word field, each letter is hashed
        String word = "ABC1234";
        Map<String, String> letters = new HashMap<>();
        letters.put("salt", salt);
        for (int i = 0; i < word.length(); i++) {
            letters.put(String.valueOf(i), DigestUtils.sha256Hex(String.valueOf(word.charAt(i)) + salt));
        }

        user.setSecretWord(new JSONObject(letters).toString());
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        Map<String, String> entered = new HashMap<>();
        entered.put("2", "X");
        entered.put("6", "4");

        authenticationService.checkLettersAgainstSecretWord(user, entered, true);
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testCheckSecretWord_notSet()
            throws ResourceNotFoundException, ResourceForbiddenException, NoSuchAlgorithmException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        Map<String, String> entered = new HashMap<>();
        entered.put("2", "X");
        entered.put("6", "4");

        authenticationService.checkLettersAgainstSecretWord(user, entered, true);
    }

    @Test
    public void testGetUserInformation() throws ResourceNotFoundException, ResourceForbiddenException {
        String token = "abc123456";
        Group group = TestUtils.createGroup("testGroup");
        group.getGroupFeatures().add(
                TestUtils.createGroupFeature(TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);

        List<Group> userGroups = new ArrayList<>();
        userGroups.add(group);

        org.patientview.api.model.UserToken input = new org.patientview.api.model.UserToken();
        input.setToken(token);

        UserToken foundUserToken = new UserToken();
        foundUserToken.setUser(user);
        foundUserToken.setToken(token);

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(externalStandardRepository.findAll()).thenReturn(new ArrayList<ExternalStandard>());
        when(groupService.getAllUserGroupsAllDetails(eq(foundUserToken.getUser().getId()))).thenReturn(userGroups);
        when(userTokenRepository.findByToken(eq(input.getToken()))).thenReturn(foundUserToken);

        org.patientview.api.model.UserToken userToken = authenticationService.getUserInformation(input);

        Assert.assertNotNull("UserToken must not be null", userToken);
        Assert.assertNotNull("token must not be null", userToken.getToken());
        Assert.assertTrue("group messaging should be set", userToken.isGroupMessagingEnabled());
        Assert.assertTrue("UserToken mustSetSecretWord should be true", userToken.isMustSetSecretWord());

        verify(externalStandardRepository, times(1)).findAll();
        verify(groupService, times(1)).getAllUserGroupsAllDetails(eq(foundUserToken.getUser().getId()));
    }

    @Test
    public void testGetUserInformation_Patient() throws Exception {
        String token = "abc123456";
        Group group = TestUtils.createGroup("testGroup");
        group.getGroupFeatures().add(
                TestUtils.createGroupFeature(TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));
        Role role = TestUtils.createRole(RoleName.PATIENT, RoleType.PATIENT);
        User user = TestUtils.createUser("testUser");

        // ENTER_OWN_DIAGNOSES group feature
        group.setGroupFeatures(new HashSet<GroupFeature>());
        group.getGroupFeatures().add(TestUtils.createGroupFeature(
                TestUtils.createFeature(FeatureType.ENTER_OWN_DIAGNOSES.toString()), group));

        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);

        List<Group> userGroups = new ArrayList<>();
        userGroups.add(group);

        org.patientview.api.model.UserToken input = new org.patientview.api.model.UserToken();
        input.setToken(token);

        UserToken foundUserToken = new UserToken();
        foundUserToken.setUser(user);
        foundUserToken.setToken(token);

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupService.getAllUserGroupsAllDetails(eq(foundUserToken.getUser().getId()))).thenReturn(userGroups);
        when(userTokenRepository.findByToken(eq(input.getToken()))).thenReturn(foundUserToken);
        when(apiConditionService.hasAnyConditions(eq(foundUserToken.getUser().getId()), eq(Boolean.TRUE))).
                thenReturn(Boolean.FALSE);

        org.patientview.api.model.UserToken userToken = authenticationService.getUserInformation(input);

        Assert.assertNotNull("UserToken must not be null", userToken);
        Assert.assertNotNull("token must not be null", userToken.getToken());
        Assert.assertTrue("group messaging should be set", userToken.isGroupMessagingEnabled());
        Assert.assertTrue("user should have shouldEnterCondition as true", userToken.isShouldEnterCondition());

        verify(groupService, times(1)).getAllUserGroupsAllDetails(eq(foundUserToken.getUser().getId()));
        verify(apiConditionService, times(1)).hasAnyConditions(
                eq(foundUserToken.getUser().getId()), eq(true));
    }

    @Test
    public void testGetUserInformation_enteredSecretWord()
            throws ResourceNotFoundException, ResourceForbiddenException {
        String token = "abc123456";
        String secretWordToken = "secretabc123456";

        User user = TestUtils.createUser("testUser");
        String salt = "saltsaltsalt";
        user.setSecretWord("{"
                + "\"salt\" : \"" + salt + "\", "
                + "\"0\" : \"" + DigestUtils.sha256Hex("A" + salt) + "\", "
                + "\"1\" : \"" + DigestUtils.sha256Hex("B" + salt) + "\", "
                + "\"2\" : \"" + DigestUtils.sha256Hex("C" + salt) + "\", "
                + "\"3\" : \"" + DigestUtils.sha256Hex("D" + salt) + "\" "
                + "}");

        Group group = TestUtils.createGroup("testGroup");
        group.getGroupFeatures().add(
                TestUtils.createGroupFeature(TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);

        List<Group> userGroups = new ArrayList<>();
        userGroups.add(group);

        org.patientview.api.model.UserToken input = new org.patientview.api.model.UserToken();
        input.setToken(token);
        input.setSecretWordChoices(new HashMap<String, String>());
        input.getSecretWordChoices().put("0", "A");
        input.getSecretWordChoices().put("2", "C");
        input.setSecretWordToken(secretWordToken);

        UserToken foundUserToken = new UserToken();
        foundUserToken.setUser(user);
        foundUserToken.setToken(token);
        foundUserToken.setCheckSecretWord(true);
        foundUserToken.setSecretWordToken(secretWordToken);

        when(externalStandardRepository.findAll()).thenReturn(new ArrayList<ExternalStandard>());
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(userTokenRepository.findBySecretWordToken(eq(input.getSecretWordToken()))).thenReturn(foundUserToken);
        when(groupService.getAllUserGroupsAllDetails(eq(foundUserToken.getUser().getId()))).thenReturn(userGroups);

        org.patientview.api.model.UserToken userToken = authenticationService.getUserInformation(input);

        Assert.assertNotNull("UserToken must not be null", userToken);
        Assert.assertNotNull("token must not be null", userToken.getToken());

        verify(externalStandardRepository, times(1)).findAll();
        verify(groupService, times(1)).getAllUserGroupsAllDetails(eq(foundUserToken.getUser().getId()));
        verify(userTokenRepository, times(1)).save(eq(foundUserToken));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testGetUserInformation_enteredSecretWordMobile()
            throws ResourceNotFoundException, ResourceForbiddenException {
        String token = "abc123456";
        String secretWordToken = "secretabc123456";

        User user = TestUtils.createUser("testUser");
        String salt = "saltsaltsalt";
        user.setSecretWord("{"
                + "\"salt\" : \"" + salt + "\", "
                + "\"0\" : \"" + DigestUtils.sha256Hex("A" + salt) + "\", "
                + "\"1\" : \"" + DigestUtils.sha256Hex("B" + salt) + "\", "
                + "\"2\" : \"" + DigestUtils.sha256Hex("C" + salt) + "\", "
                + "\"3\" : \"" + DigestUtils.sha256Hex("D" + salt) + "\" "
                + "}");

        Group group = TestUtils.createGroup("testGroup");
        group.getGroupFeatures().add(
                TestUtils.createGroupFeature(TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);

        List<Group> userGroups = new ArrayList<>();
        userGroups.add(group);

        org.patientview.api.model.UserToken input = new org.patientview.api.model.UserToken();
        input.setToken(token);
        input.setSecretWord("ABCD");
        input.setSecretWordToken(secretWordToken);

        UserToken foundUserToken = new UserToken();
        foundUserToken.setUser(user);
        foundUserToken.setToken(token);
        foundUserToken.setCheckSecretWord(true);
        foundUserToken.setSecretWordToken(secretWordToken);
        foundUserToken.setType(UserTokenTypes.MOBILE);

        when(externalStandardRepository.findAll()).thenReturn(new ArrayList<ExternalStandard>());
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(userTokenRepository.findBySecretWordToken(eq(input.getSecretWordToken()))).thenReturn(foundUserToken);
        when(groupService.getAllUserGroupsAllDetails(eq(foundUserToken.getUser().getId()))).thenReturn(userGroups);

        org.patientview.api.model.UserToken userToken = authenticationService.getUserInformation(input);

        Assert.assertNotNull("UserToken must not be null", userToken);
        Assert.assertNotNull("token must not be null", userToken.getToken());
        Assert.assertNotNull("token must not be null", userToken.getSecretWordSalt());

        verify(externalStandardRepository, times(1)).findAll();
        verify(groupService, times(1)).getAllUserGroupsAllDetails(eq(foundUserToken.getUser().getId()));
        verify(userTokenRepository, times(1)).save(eq(foundUserToken));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testGetUserInformation_enteredWrongSecretWord()
            throws ResourceNotFoundException, ResourceForbiddenException {
        String token = "abc123456";
        String secretWordToken = "secretabc123456";

        User user = TestUtils.createUser("testUser");
        String salt = "saltsaltsalt";
        user.setSecretWord("{"
                + "\"salt\" : \"" + salt + "\", "
                + "\"0\" : \"" + DigestUtils.sha256Hex("A" + salt) + "\", "
                + "\"1\" : \"" + DigestUtils.sha256Hex("B" + salt) + "\", "
                + "\"2\" : \"" + DigestUtils.sha256Hex("C" + salt) + "\", "
                + "\"3\" : \"" + DigestUtils.sha256Hex("D" + salt) + "\" "
                + "}");

        Group group = TestUtils.createGroup("testGroup");
        group.getGroupFeatures().add(
                TestUtils.createGroupFeature(TestUtils.createFeature(FeatureType.MESSAGING.toString()), group));
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN, RoleType.STAFF);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);

        List<Group> userGroups = new ArrayList<>();
        userGroups.add(group);

        org.patientview.api.model.UserToken input = new org.patientview.api.model.UserToken();
        input.setToken(token);
        input.setSecretWordChoices(new HashMap<String, String>());
        input.getSecretWordChoices().put("0", "A");
        input.getSecretWordChoices().put("3", "X");
        input.setSecretWordToken(secretWordToken);

        UserToken foundUserToken = new UserToken();
        foundUserToken.setUser(user);
        foundUserToken.setToken(token);
        foundUserToken.setCheckSecretWord(true);
        foundUserToken.setSecretWordToken(secretWordToken);

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(userTokenRepository.findBySecretWordToken(eq(input.getSecretWordToken()))).thenReturn(foundUserToken);
        when(groupService.getAllUserGroupsAllDetails(eq(foundUserToken.getUser().getId()))).thenReturn(userGroups);

        authenticationService.getUserInformation(input);
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
        user.setDeleted(false);

        when(userRepository.findByUsernameCaseInsensitive(any(String.class))).thenReturn(user);
        authenticationService.authenticate(new Credentials(user.getUsername(), password));
    }

    @Test
    public void testLogout() throws AuthenticationServiceException {
        Date now = new Date();

        User user = new User();
        user.setUsername("testUsername");
        user.setId(1L);

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(CommonUtils.getAuthToken());
        userToken.setCreated(now);
        userToken.setExpiration(new Date(now.getTime() + 1200));

        when(userTokenRepository.findByToken(eq(userToken.getToken()))).thenReturn(userToken);

        authenticationService.logout(userToken.getToken(), false);

        verify(userTokenRepository, times(1)).findByToken(eq(userToken.getToken()));
        verify(auditService, times(1)).createAudit(eq(AuditActions.LOGGED_OFF), eq(user.getUsername()),
                eq(user), eq(user.getId()), eq(AuditObjectTypes.User), (Group) isNull());
        verify(userTokenRepository, times(1)).delete(eq(userToken));
        verify(userTokenRepository, times(1)).deleteExpiredByUserId(eq(user.getId()));
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

        when(userRepository.findById(eq(switchUser.getId()))).thenReturn(Optional.of(switchUser));
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);
        when(userService.currentUserCanSwitchToUser(eq(switchUser))).thenReturn(true);

        authenticationService.switchToUser(switchUser.getId());
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testSwitchUser_CurrentlyPatient() throws Exception {

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

        when(userRepository.findById(eq(switchUser.getId()))).thenReturn(Optional.of(switchUser));
        when(userTokenRepository.save(any(UserToken.class))).thenReturn(userToken);
        when(userService.currentUserCanSwitchToUser(eq(switchUser))).thenReturn(false);

        authenticationService.switchToUser(switchUser.getId());
    }

    @Test
    public void testSessionExpired_notExpiredWeb() {
        String token = "abc123456";
        Date expiration = new Date(new Date().getTime() + 1000000);
        Date oneYear = new DateTime(new Date()).plusYears(1).toDate();

        when(userTokenRepository.getExpiration(eq(token))).thenReturn(expiration);
        when(userTokenRepository.getType(eq(token))).thenReturn(UserTokenTypes.WEB);

        boolean expired = authenticationService.sessionExpired(token);

        Assert.assertFalse("token should not be expired", expired);

        verify(userTokenRepository, times(1)).getExpiration(eq(token));
        verify(userTokenRepository, times(1)).getType(eq(token));
        verify(userTokenRepository, times(1)).setExpiration(eq(token), dateCaptor.capture());

        Assert.assertTrue("set date of expiration should be correct", dateCaptor.getValue().after(expiration));
        Assert.assertTrue("set date of expiration should be correct", dateCaptor.getValue().before(oneYear));
    }

    @Test
    public void testSessionExpired_notExpiredMobile() {
        String token = "abc123456";
        Date expiration = new Date(new Date().getTime() + 1800000);
        Date oneYear = new DateTime(new Date()).plusYears(1).toDate();

        when(userTokenRepository.getExpiration(eq(token))).thenReturn(expiration);
        when(userTokenRepository.getType(eq(token))).thenReturn(UserTokenTypes.MOBILE);

        boolean expired = authenticationService.sessionExpired(token);

        Assert.assertFalse("token should not be expired", expired);

        verify(userTokenRepository, times(1)).getExpiration(eq(token));
        verify(userTokenRepository, times(1)).getType(eq(token));
        verify(userTokenRepository, times(1)).setExpiration(eq(token), dateCaptor.capture());

        Assert.assertTrue("set date of expiration should be correct", dateCaptor.getValue().after(oneYear));
    }

    @Test
    public void testSessionExpired_expiredWeb() {
        String token = "abc123456";
        Date expiration = new Date(new Date().getTime() - 1800000);

        when(userTokenRepository.getExpiration(eq(token))).thenReturn(expiration);

        boolean expired = authenticationService.sessionExpired(token);

        Assert.assertTrue("token should be expired", expired);

        verify(userTokenRepository, times(1)).getExpiration(eq(token));
        verify(userTokenRepository, times(0)).getType(anyString());
        verify(userTokenRepository, times(0)).setExpiration(anyString(), any(Date.class));
    }
}
