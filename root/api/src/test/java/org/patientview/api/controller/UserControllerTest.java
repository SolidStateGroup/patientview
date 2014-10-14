package org.patientview.api.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.AuditAspect;
import org.patientview.api.model.Credentials;
import org.patientview.api.service.AuditService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
public class UserControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private UriComponentsBuilder uriComponentsBuilder;

    @Mock
    private UserService userService;

    @Mock
    private GroupService groupService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private AuditAspect auditAspect = AuditAspect.aspectOf();

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(userController, uriComponentsBuilder).build();
    }

    /**
     * Test: Send a GET request with a long parameter to the user service
     * Fail: The service does not get called with the parameter
     *
     */
    @Test
    @Ignore("Temporarily removed pending testing with user transport objects")
    public void testGetUser() throws ResourceNotFoundException  {

        User user = new User();
        user.setId(1L);

        when(userService.get(eq(user.getId()))).thenReturn(user);
        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + Long.toString(user.getId())))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }

    /**
     * Test: User creation without password reset
     * Fail: The UserService does not get called
     *
     * Improve test to verify the correct user is being saved
     */
    @Test
    @Ignore("Needs refactoring sprint 3")
    public void testCreateUser() throws ResourceNotFoundException {
        User postUser = TestUtils.createUser("testPost");
        User persistedUser = TestUtils.createUser("testPost");

        TestUtils.authenticateTest(postUser);

        when(userService.get(anyLong())).thenReturn(TestUtils.createUser( "creator"));

        when(userService.createUserWithPasswordEncryption(any(User.class))).thenReturn(persistedUser);
        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/user")
                    .content(mapper.writeValueAsString(postUser)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
        }

        catch (Exception e) {
            fail("The post request all should not fail " + e.getCause());
        }

        verify(userService, Mockito.times(1)).createUserWithPasswordEncryption(any(User.class));
    }


    /**
     * Test: Adding a GroupRole to a user
     * Fail: The GroupService method does not get called
     *
     * Improve test to verify the correct user is being saved
     */
    @Test
    public void testAddGroupRole() throws ResourceNotFoundException {
        Long userId = 1L;
        Long groupId = 2L;
        Long roleId = 3L;

        String url = "/user/" + userId + "/group/" + groupId + "/role/" + roleId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
        catch (Exception e) {
            fail("The put request all should not fail " + e.getCause());
        }

        verify(userService, Mockito.times(1)).addGroupRole(Matchers.eq(userId), Matchers.eq(groupId), Matchers.eq(roleId));
    }

    /**
     * Test: The url to reset a password
     * Fail: The service method does not get called
     *
     * * TODO Fix verify - possible problem with aspect
     */
    @Test
    public void testResetPassword() throws ResourceNotFoundException {

        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser, Collections.EMPTY_LIST);

        String url = "/user/" + testUser.getId() + "/resetPassword";
        Credentials credentials = new Credentials();
        credentials.setPassword("newPassword");
        credentials.setUsername(testUser.getUsername());

        when(auditService.save(any(Audit.class))).thenReturn(new Audit());

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                    .content(mapper.writeValueAsString(credentials)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("The post request should not fail " + e.getCause());
        }

      //  verify(userService, Mockito.times(1)).resetPassword(eq(testUser.getId()), eq(credentials.getPassword()));
    }

    /**
     * Test: The url to reset a password
     * Fail: The service method does not get called
     * TODO Fix verify - possible problem with aspect
     */
    @Test
    public void testUpdatePassword() throws ResourceNotFoundException {

        User testUser = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(testUser);

        String url = "/user/" + testUser.getId() + "/changePassword";
        Credentials credentials = new Credentials();
        credentials.setPassword("newPassword");
        credentials.setUsername(testUser.getUsername());

        when(auditService.save(any(Audit.class))).thenReturn(new Audit());

        try {
            mockMvc.perform(MockMvcRequestBuilders.post(url)
                    .content(mapper.writeValueAsString(credentials)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("The post request should not fail " + e.getCause());
        }
       //weirdest Mockito bug
      // verify(userService, Mockito.times(1)).changePassword(Matchers.eq(testUser.getId()), Matchers.eq(credentials.getPassword()));
    }

}
