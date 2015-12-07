package org.patientview.api.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.ConditionService;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by james@solidstategroup.com
 * Created on 03/12/2015
 */
public class DiagnosisControllerTest {

    @Mock
    private ConditionService conditionService;

    @InjectMocks
    private DiagnosisController diagnosisController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(diagnosisController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testAdd() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        Long userId = 1L;
        String code = "00";

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + userId + "/diagnosis/" + code))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(conditionService, Mockito.times(1)).staffAddCondition(eq(userId), eq(code));
    }

    @Test
    public void testGetStaffEntered() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        Long userId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + userId + "/diagnosis/staffentered")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
