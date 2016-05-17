package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.SurveyResponseService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.fail;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/06/2015
 */
public class SurveyResponseControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private SurveyResponseService surveyResponseService;

    @InjectMocks
    private SurveyResponseController surveyResponseController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(surveyResponseController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testAdd() {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/user/" + user.getId() + "/surveyresponses")
                    .content(mapper.writeValueAsString(new SurveyResponse())).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }

    @Test
    public void testGetSurveyResponsesByUserIdAndType() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/user/" + user.getId() + "/surveyresponses/type/" + SurveyTypes.CROHNS_SYMPTOM_SCORE))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetLatestSurveyResponsesByUserIdAndType() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders
                .get("/user/" + user.getId() + "/surveyresponses/latest?" + SurveyTypes.CROHNS_SYMPTOM_SCORE))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}


