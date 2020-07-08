package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
public class ObservationHeadingControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ObservationHeadingService observationHeadingService;

    @Mock
    private ObservationHeadingRepository observationHeadingRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private ObservationHeadingController observationHeadingController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(observationHeadingController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testFindAll() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        mockMvc.perform(MockMvcRequestBuilders
                .get("/observationheadings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1)).findAll();
    }

    @Test
    public void testFindAllPaged() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        mockMvc.perform(MockMvcRequestBuilders
                .get("/observationheading?page=0&size=5&sortDirection=ASC&sortField=name")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testGet() throws Exception {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);

        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        mockMvc.perform(MockMvcRequestBuilders.get("/observationheading/" + observationHeading.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1)).get(eq(observationHeading.getId()));
    }

    @Test
    public void testAdd() throws Exception {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);

        mockMvc.perform(MockMvcRequestBuilders.post("/observationheading")
                .content(mapper.writeValueAsString(observationHeading))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1)).add(eq(observationHeading));
    }

    @Test
    public void testSave() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);

        mockMvc.perform(MockMvcRequestBuilders.put("/observationheading")
                .content(mapper.writeValueAsString(observationHeading))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testAddGroup() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);
        Group group = TestUtils.createGroup("GROUP1");
        group.setId(2L);

        when(observationHeadingRepository.findById(eq(observationHeading.getId())).get())
                .thenReturn(observationHeading);
        when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);
        when(groupRepository.findById(eq(group.getId())).get()).thenReturn(group);

        mockMvc.perform(MockMvcRequestBuilders.post("/observationheading/" + observationHeading.getId() + "/group/"
                + group.getId() + "/panel/3/panelorder/4"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1)).addObservationHeadingGroup(eq(observationHeading.getId()),
                eq(group.getId()), eq(3L), eq(4L));
    }

    @Test
    public void testUpdateGroup() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);

        mockMvc.perform(MockMvcRequestBuilders.put("/observationheadinggroup/")
                .content(mapper.writeValueAsString(new org.patientview.api.model.ObservationHeadingGroup()))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1))
                .updateObservationHeadingGroup(any(org.patientview.api.model.ObservationHeadingGroup.class));
    }

    @Test
    public void testRemoveGroup() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);
        Group group = TestUtils.createGroup("GROUP1");
        group.setId(2L);

        ObservationHeadingGroup observationHeadingGroup
                = new ObservationHeadingGroup(observationHeading, group, 3L, 4L);
        observationHeadingGroup.setId(3L);
        observationHeading.getObservationHeadingGroups().add(observationHeadingGroup);

        when(observationHeadingRepository.findById(eq(observationHeading.getId())).get())
                .thenReturn(observationHeading);
        when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);
        when(groupRepository.findById(eq(group.getId())).get()).thenReturn(group);

        mockMvc.perform(MockMvcRequestBuilders.delete("/observationheadinggroup/"
                + observationHeadingGroup.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1))
                .removeObservationHeadingGroup(eq(observationHeadingGroup.getId()));
    }

    @Test
    public void testGetResultClusters() throws Exception {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.PATIENT);

        mockMvc.perform(MockMvcRequestBuilders.get("/resultclusters")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1)).getResultClusters();
    }

    @Test
    public void testGetAvailableResultTypes() throws Exception {

        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/availableobservationheadings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1)).getAvailableObservationHeadings(user.getId());
    }

    @Test
    public void testGetSavedResultTypes() throws Exception {

        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/savedobservationheadings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1)).getSavedObservationHeadings(user.getId());
    }

    @Test
    public void testGetAvailableAlertObservationHeadings() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/availablealertobservationheadings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1)).getAvailableAlertObservationHeadings(user.getId());
    }

    @Test
    public void testGetPatientEnteredObservationHeadings() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/patiententeredobservationheadings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(observationHeadingService, Mockito.times(1)).getPatientEnteredObservationHeadings(user.getId());
    }
}
