package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.ApiConditionService;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by james@solidstategroup.com
 * Created on 03/12/2015
 */
public class DiagnosisControllerTest {

    @Mock
    private ApiConditionService apiConditionService;

    @InjectMocks
    private DiagnosisController diagnosisController;

    private MockMvc mockMvc;

    private ObjectMapper mapper = new ObjectMapper();

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
    public void testAddMultiplePatientEntered() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        List<String> codes = new ArrayList<>();
        codes.add("00");
        codes.add("01");

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + user.getId() + "/diagnosis/patiententered")
                .content(mapper.writeValueAsString(codes)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    @Test
    public void testAddPatientEntered() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        String code = "00";

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + user.getId() + "/diagnosis/" + code + "/patiententered"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        //verify(apiConditionService, Mockito.times(1)).patientAddCondition(eq(user.getId()), eq(code));
    }

    @Test
    public void testAddStaffEntered() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        Long userId = 1L;
        String code = "00";

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + userId + "/diagnosis/" + code + "/staffentered"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(apiConditionService, Mockito.times(1)).staffAddCondition(eq(userId), eq(code));
    }

    @Test
    public void testGetStaffEntered() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        Long userId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + userId + "/diagnosis/staffentered")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testRemoveStaffEntered() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        Long userId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/user/" + userId + "/diagnosis/staffentered")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
