package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.Credentials;
import org.patientview.api.model.ForgottenCredentials;
import org.patientview.api.model.UserToken;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.UserService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationService authenticationService;

    private ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    private String token;

    @Mock
    private UserService userService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        this.token = "ABC123";
    }

    @Test
    public void testGetBasicUserInformation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/" + token + "/basicuserinformation/"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).getBasicUserInformation(eq(token));
    }

    @Test
    public void testAuthenticate() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setUsername("testUser");
        credentials.setPassword("doNotShow");

        when(authenticationService.authenticate(eq(credentials))).thenReturn(new UserToken("1234"));
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .content(mapper.writeValueAsString(credentials)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(authenticationService, Mockito.times(1)).authenticate(any(Credentials.class));
    }

    @Test
    public void testGetUserInformation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/userinformation")
                .content(mapper.writeValueAsString(new UserToken(token))).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).getUserInformation(any(UserToken.class));
    }

    /**
     * Test: The url for resetting a password from a Username and Email
     * Fail: The service method is not called
     *
     */
    @Test
    public void testForgottenPassword() throws Exception {
        ForgottenCredentials forgottenCredentials = new ForgottenCredentials();
        forgottenCredentials.setEmail("rememberedEmail");
        forgottenCredentials.setUsername("rememberedUsername");

        String url = "/auth/forgottenpassword";

        mockMvc.perform(MockMvcRequestBuilders.post(url)
                .content(mapper.writeValueAsString(forgottenCredentials)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(userService, Mockito.times(1))
                .resetPasswordByUsernameAndEmail(
                        Matchers.eq(forgottenCredentials.getUsername()), Matchers.eq(forgottenCredentials.getEmail()));
    }

    /**
     * Test: Call the /auth/login and pass the username and password as json
     * Fail: The username and password are not passed into the service layer.
     *
     */
    @Test
    public void testLogin() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setUsername("testUser");
        credentials.setPassword("doNotShow");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .content(mapper.writeValueAsString(credentials)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).authenticate(any(Credentials.class));
    }

    @Test
    public void testLoginMobile() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setUsername("testUser");
        credentials.setPassword("doNotShow");

        mockMvc.perform(MockMvcRequestBuilders.post("/auth/loginmobile")
                .content(mapper.writeValueAsString(credentials)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).authenticateMobile(any(Credentials.class), any(Boolean.class));
    }

    @Test
    public void testLogout() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/auth/logout/" + token))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).logout(eq(token), eq(false));
    }

    @Test
    public void testSwitchUser() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/switchuser/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).switchToUser(eq(1L));
    }

    @Test
    public void testSwitchToPreviousUser() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/auth/" + token + "/switchuser/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).switchBackFromUser(eq(1L), eq(token));
    }

    @Test
    public void testTestService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/status"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
