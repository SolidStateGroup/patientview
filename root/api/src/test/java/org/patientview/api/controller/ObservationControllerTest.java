package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.IdValue;
import org.patientview.api.model.UserResultCluster;
import org.patientview.api.service.ApiObservationService;
import org.patientview.persistence.model.FhirObservationRange;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.ObservationHeading;
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
public class ObservationControllerTest {

    @Mock
    private ApiObservationService apiObservationService;

    @InjectMocks
    private ObservationController observationController;

    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(observationController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetObservationsByUserIdAndCode() {
        User user = TestUtils.createUser("testuser");
        String code = "EXAMPLE_CODE";

        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/observations/" + code))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testGetObservationSummaryByUserId() {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/observations/summary"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }

    @Test
    public void testPostResultSummary() {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        ObservationHeading observationHeading = new ObservationHeading();
        observationHeading.setId(2L);
        observationHeading.setCode("EXAMPLE_CODE");

        UserResultCluster userResultCluster = new UserResultCluster();
        userResultCluster.setValues(new ArrayList<IdValue>());
        IdValue value = new IdValue();
        value.setId(observationHeading.getId());
        value.setValue("99.9");

        List<UserResultCluster> userResultClusters = new ArrayList<>();
        userResultClusters.add(userResultCluster);

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/user/" + user.getId() + "/observations/resultclusters")
                    .content(mapper.writeValueAsString(userResultClusters)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }

    @Test
    public void testPostObservations() throws Exception {
        User user = TestUtils.createUser("testUser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN_API);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        User patient = TestUtils.createUser("testPatient");

        FhirObservationRange fhirObservationRange = new FhirObservationRange();
        fhirObservationRange.setCode("wbc");
        fhirObservationRange.setStartDate(new Date());
        fhirObservationRange.setEndDate(new Date());
        fhirObservationRange.setObservations(new ArrayList<org.patientview.persistence.model.FhirObservation>());

        mockMvc.perform(MockMvcRequestBuilders
                .post("/user/" + patient.getId() + "/group/" + group.getId() + "/observations")
                .content(mapper.writeValueAsString(fhirObservationRange)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
                
        verify(apiObservationService, Mockito.times(1))
                .addTestObservations(eq(patient.getId()), eq(group.getId()), any(FhirObservationRange.class));
    }

    @Test
    public void testPatientEnteredObservationsByCode() {
        User user = TestUtils.createUser("testuser");
        String code = "EXAMPLE_CODE";

        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            mockMvc.perform(MockMvcRequestBuilders
                    .get("/user/" + user.getId() + "/observations/" + code + "/patiententered"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testUpdatePatientEnteredResult() throws Exception {
        User patient = TestUtils.createUser("testPatient");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, patient);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(patient, groupRoles);

        FhirObservation fhirObservation = new FhirObservation();
        UUID uuid = UUID.randomUUID();
        fhirObservation.setLogicalId(uuid);
        fhirObservation.setApplies(new Date());
        fhirObservation.setValue("60");

        mockMvc.perform(MockMvcRequestBuilders
                .post("/user/" + patient.getId() + "/observations/patiententered")
                .content(mapper.writeValueAsString(fhirObservation)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testDeletePatientEnteredResult() throws Exception {
        User user = TestUtils.createUser("testUser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        User patient = TestUtils.createUser("testPatient");

        UUID uuid = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders
                .delete("/user/" + patient.getId() + "/observations/"+uuid.toString()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
