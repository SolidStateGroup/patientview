package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.SurveyFeedbackService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.patientview.service.SurveyService;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/05/2016
 */
public class SurveyControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private SurveyService surveyService;

    @Mock
    private SurveyFeedbackService surveyFeedbackService;

    @InjectMocks
    private SurveyController surveyController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(surveyController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetByType() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        String type = SurveyTypes.CROHNS_SYMPTOM_SCORE.toString();

        mockMvc.perform(MockMvcRequestBuilders.get("/surveys/type/" + type)
                .content(mapper.writeValueAsString(new SurveyResponse())).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(surveyService, Mockito.times(1)).getByType(eq(type));
    }

    @Test
    public void testGetFeedback() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/1/surveys/2/feedback")
                .content(mapper.writeValueAsString(new SurveyResponse())).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
