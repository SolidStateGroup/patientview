package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.ApiObservationService;
import org.patientview.api.service.ApiPatientService;
import org.patientview.persistence.model.FhirObservationRange;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
public class ImportControllerTest {

    @Mock
    private ApiObservationService apiObservationService;

    @Mock
    private ApiPatientService apiPatientService;

    @InjectMocks
    private ImportController importController;

    private ObjectMapper mapper = new ObjectMapper();

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
    public void testImportObservations() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        FhirObservationRange fhirObservationRange = new FhirObservationRange();

        mockMvc.perform(MockMvcRequestBuilders.post("/import/observations")
                .content(mapper.writeValueAsString(fhirObservationRange)).contentType(MediaType.APPLICATION_JSON))
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

        FhirPatient fhirPatient = new FhirPatient();

        mockMvc.perform(MockMvcRequestBuilders.post("/import/patient")
                .content(mapper.writeValueAsString(fhirPatient)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(apiPatientService, Mockito.times(1)).importPatient(any(FhirPatient.class));
    }

}
