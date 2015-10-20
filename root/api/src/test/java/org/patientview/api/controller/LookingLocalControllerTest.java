package org.patientview.api.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.LookingLocalRoutes;
import org.patientview.api.service.LookingLocalService;
import org.patientview.test.util.TestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 20/10/2015
 */
public class LookingLocalControllerTest {

    @Mock
    private LookingLocalService lookingLocalService;

    @InjectMocks
    private LookingLocalController lookingLocalController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(lookingLocalController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetHome() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(LookingLocalRoutes.LOOKING_LOCAL_HOME))
                .andExpect(MockMvcResultMatchers.status().isOk());

    }
}


