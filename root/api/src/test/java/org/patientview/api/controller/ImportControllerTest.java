package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.Credentials;
import org.patientview.api.service.ApiDiagnosticService;
import org.patientview.api.service.ApiMedicationService;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.ApiPractitionerService;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.ClinicalDataService;
import org.patientview.api.service.LetterService;
import org.patientview.api.service.PatientManagementService;
import org.patientview.persistence.model.FhirClinicalData;
import org.patientview.persistence.model.FhirDiagnosticReportRange;
import org.patientview.persistence.model.FhirDocumentReference;
import org.patientview.persistence.model.FhirMedicationStatementRange;
import org.patientview.persistence.model.FhirObservationRange;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
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

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
public class ImportControllerTest {

    @Mock
    private ApiDiagnosticService apiDiagnosticService;

    @Mock
    private ApiMedicationService apiMedicationService;

    @Mock
    private ApiObservationService apiObservationService;

    @Mock
    private ApiPatientService apiPatientService;

    @Mock
    private ApiPractitionerService apiPractitionerService;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private ClinicalDataService clinicalDataService;

    @InjectMocks
    private ImportController importController;

    @Mock
    private LetterService letterService;

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private PatientManagementService patientManagementService;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(importController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testImportClinicalData() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.post("/import/clinicaldata")
                .content(mapper.writeValueAsString(new FhirClinicalData()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(clinicalDataService, Mockito.times(1)).importClinicalData(any(FhirClinicalData.class));
    }

    @Test
    public void testImportDiagnostics() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.post("/import/diagnostics")
                .content(mapper.writeValueAsString(new FhirDiagnosticReportRange()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(apiDiagnosticService, Mockito.times(1)).importDiagnostics(any(FhirDiagnosticReportRange.class));
    }

    @Test
    public void testImportLetter() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.post("/import/letter")
                .content(mapper.writeValueAsString(new FhirDocumentReference()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(letterService, Mockito.times(1)).importLetter(any(FhirDocumentReference.class));
    }

    @Test
    public void testImportLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/import/login")
                .content(mapper.writeValueAsString(new Credentials()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(authenticationService, Mockito.times(1)).authenticateImporter(any(Credentials.class));
    }

    @Test
    public void testImportMedication() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.post("/import/medication")
                .content(mapper.writeValueAsString(new FhirMedicationStatementRange()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(apiMedicationService, Mockito.times(1)).importMedication(any(FhirMedicationStatementRange.class));
    }

    @Test
    public void testImportObservations() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.post("/import/observations")
                .content(mapper.writeValueAsString(new FhirObservationRange())).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(apiObservationService, Mockito.times(1)).importObservations(any(FhirObservationRange.class));
    }

    @Test
    public void testImportPatient() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.post("/import/patient")
                .content(mapper.writeValueAsString(new FhirPatient())).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(apiPatientService, Mockito.times(1)).importPatient(any(FhirPatient.class));
    }

    @Test
    public void testImportPatientManagement() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.post("/import/patientmanagement")
                .content(mapper.writeValueAsString(new PatientManagement())).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(patientManagementService, Mockito.times(1)).importPatientManagement(any(PatientManagement.class));
    }

    @Test
    public void testImportPractitioner() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.post("/import/practitioner")
                .content(mapper.writeValueAsString(new FhirPractitioner())).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(apiPractitionerService, Mockito.times(1)).importPractitioner(any(FhirPractitioner.class));
    }
}
