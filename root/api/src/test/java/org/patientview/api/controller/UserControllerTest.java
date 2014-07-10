package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.UserService;
import org.patientview.persistence.model.User;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriComponentsBuilder;

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

    @InjectMocks
    private UserController userController;

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
    public void testGetUser() {

        Long testUserId = 10L;

        when(userService.getUser(eq(testUserId))).thenReturn(new User());;
        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + Long.toString(testUserId)))
                    .andExpect(MockMvcResultMatchers.status().isOk());;
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }
    }

    /**
     * Test: User creation without password reset
     * Fail: The UserService does not get called
     *
     * Improve test to verify the correct user is being saved
     */
    @Test
    public void testCreateUser()  {
        User postUser = TestUtils.createUser(null, "testPost");

        when(userService.getUser(anyLong())).thenReturn(TestUtils.createUser(1L, "creator"));
        when(userService.createUserWithPasswordEncryption(any(User.class))).thenReturn(postUser);
        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/user")
                    .content(mapper.writeValueAsString(postUser)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
        }

        catch (Exception e) {
            Assert.fail("The post request all should not fail " + e.getCause());
        }

        verify(userService, Mockito.times(1)).createUserWithPasswordEncryption(any(User.class));
    }

    /**
     * Test: User creation with password reset
     * Fail: The UserService does not get called
     *
     * Improve test to verify the correct user is being saved
     */
    @Test
    public void testCreateUserWithPasswordReset()  {
        User postUser = TestUtils.createUser(null, "testPost");

        when(userService.getUser(anyLong())).thenReturn(TestUtils.createUser(1L, "creator"));
        when(userService.createUserResetPassword(any(User.class))).thenReturn(postUser);
        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/user?resetPassword=true")
                    .content(mapper.writeValueAsString(postUser)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
        }

        catch (Exception e) {
            Assert.fail("The post request all should not fail " + e.getCause());
        }

        verify(userService, Mockito.times(1)).createUserResetPassword(any(User.class));
    }

}
