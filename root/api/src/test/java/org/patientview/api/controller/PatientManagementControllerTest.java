package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.CodeService;
import org.patientview.api.service.LookupService;
import org.patientview.api.service.PatientManagementService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.PatientManagement;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by james@solidstategroup.com
 * Created on 29/03/2016
 */
public class PatientManagementControllerTest {

    @Mock
    private CodeService codeService;

    @Mock
    private PatientManagementService patientManagementService;

    @Mock
    private LookupService lookupService;

    private ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @InjectMocks
    private PatientManagementController patientManagementController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(patientManagementController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetPatientManagementDiagnoses() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/patientmanagement/diagnoses"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(codeService, times(1)).getPatientManagementDiagnoses();
    }

    @Test
    public void testGetPatientManagementLookupTypes() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/patientmanagement/lookuptypes"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(lookupService, times(1)).getPatientManagementLookupTypes();
    }
    
    @Test
    public void testGetPatientManagement() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/patientmanagement/1/group/2/identifier/3"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(patientManagementService, times(1)).get(eq(1L), eq(2L), eq(3L));
    }

    @Test
    public void testSavePatientManagement() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.getGroupRoles().add(TestUtils.createGroupRole(role, group, user));
        TestUtils.authenticateTest(user, user.getGroupRoles());

        mockMvc.perform(MockMvcRequestBuilders.post("/patientmanagement/1/group/2/identifier/3")
                .content(mapper.writeValueAsString(new PatientManagement())).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(patientManagementService, times(1)).save(eq(1L), eq(2L), eq(3L), any(PatientManagement.class));
    }

    @Test
    public void testValidatePatientManagement() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.getGroupRoles().add(TestUtils.createGroupRole(role, group, user));
        TestUtils.authenticateTest(user, user.getGroupRoles());

        mockMvc.perform(MockMvcRequestBuilders.post("/patientmanagement/validate")
                .content(mapper.writeValueAsString(new PatientManagement())).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(patientManagementService, times(1)).validate(any(PatientManagement.class));
    }
}
