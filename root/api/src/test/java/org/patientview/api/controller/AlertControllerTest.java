package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.BaseUser;
import org.patientview.api.service.AlertService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/01/2015
 */
public class AlertControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertController alertController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(alertController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testAddAlert() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");

        org.patientview.api.model.Alert alert = new org.patientview.api.model.Alert();
        alert.setUser(new BaseUser(user));
        alert.setObservationHeading(new org.patientview.api.model.ObservationHeading(observationHeading));
        alert.setWebAlert(true);
        alert.setWebAlertViewed(false);
        alert.setEmailAlert(true);
        alert.setEmailAlertSent(false);
        alert.setAlertType(AlertTypes.RESULT);

        mockMvc.perform(MockMvcRequestBuilders.post("/user/" + user.getId() + "/alert")
                .content(mapper.writeValueAsString(alert))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetAlerts() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        AlertTypes alertType = AlertTypes.RESULT;

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/alerts/" + alertType.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(alertService, Mockito.times(1)).getAlerts(user.getId(), alertType);
    }

    @Test
    public void testGetContactAlerts() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/contactalerts/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(alertService, Mockito.times(1)).getContactAlerts(user.getId());
    }

    @Test
    public void testGetImportAlerts() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/importalerts/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(alertService, Mockito.times(1)).getImportAlerts(user.getId());
    }

    @Test
    public void testRemoveAlert() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");

        org.patientview.api.model.Alert alert = new org.patientview.api.model.Alert();
        alert.setId(1L);
        alert.setUser(new BaseUser(user));
        alert.setObservationHeading(new org.patientview.api.model.ObservationHeading(observationHeading));
        alert.setWebAlert(true);
        alert.setWebAlertViewed(false);
        alert.setEmailAlert(true);
        alert.setEmailAlertSent(false);
        alert.setAlertType(AlertTypes.RESULT);

        String url = "/user/" + user.getId() + "/alerts/" + alert.getId();
        mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testUpdateAlert() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");

        org.patientview.api.model.Alert alert = new org.patientview.api.model.Alert();
        alert.setId(1L);
        alert.setUser(new BaseUser(user));
        alert.setObservationHeading(new org.patientview.api.model.ObservationHeading(observationHeading));
        alert.setWebAlert(true);
        alert.setWebAlertViewed(false);
        alert.setEmailAlert(true);
        alert.setEmailAlertSent(false);
        alert.setAlertType(AlertTypes.RESULT);

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + user.getId() + "/alert")
                .content(mapper.writeValueAsString(alert))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
