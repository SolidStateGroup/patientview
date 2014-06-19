package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.AdminService;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.UserService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Base test controller for creating a controller ready to be tested.
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Ignore
public class BaseControllerTest<T extends BaseController> {

    private ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UserService userService;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private T controller;

    @Before
    public void setup() {

        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public T getController() {
        return controller;
    }

    public void setController(final T controller) {
        this.controller = controller;
    }

    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public void setAuthenticationService(final AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    public AdminService getAdminService() {
        return adminService;
    }

    public void setAdminService(final AdminService adminService) {
        this.adminService = adminService;
    }

    public MockMvc getMockMvc() {
        return mockMvc;
    }

    public void setMockMvc(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }
}
