package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.LookupService;
import org.patientview.persistence.model.FhirPatient;
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

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by james@solidstategroup.com
 * Created on 03/09/2014
 */
public class PatientControllerTest {

    @Mock
    private ApiPatientService apiPatientService;

    @Mock
    private LookupService lookupService;

    private ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @InjectMocks
    private PatientController patientController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(patientController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGet() throws Exception {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/patient/" + user.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGetPatientManagementLookupTypes() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/patientmanagement/lookuptypes"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(lookupService, times(1)).getPatientManagementLookupTypes();
    }
    
    @Test
    public void testUpdate() throws Exception {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN_API);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);
        
        FhirPatient fhirPatient = new FhirPatient();
        fhirPatient.setForename("forename");
        fhirPatient.setSurname("surname");

        mockMvc.perform(MockMvcRequestBuilders.put("/patient/" + user.getId() + "/group/" + group.getId())
                .content(mapper.writeValueAsString(fhirPatient)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        //verify(apiPatientService, Mockito.times(1)).update(eq(user.getId()), eq(group.getId()), eq(fhirPatient));
    }
}
