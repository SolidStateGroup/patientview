package org.patientview.api.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.NhsChoicesService;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/01/2016
 */
public class NhsChoicesControllerTest {

    @Mock
    private NhsChoicesService nhsChoicesService;

    @InjectMocks
    private NhsChoicesController nhsChoicesController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(nhsChoicesController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testConditionsUpdate() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.post("/nhschoices/conditions/update"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testOrganisationsUpdate() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.post("/nhschoices/organisations/update"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
