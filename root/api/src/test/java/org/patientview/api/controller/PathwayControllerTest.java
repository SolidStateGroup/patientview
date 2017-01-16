package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.Pathway;
import org.patientview.api.model.Stage;
import org.patientview.api.service.PathwayService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.verify;

/**
 * Unit test for Note endpoints
 */
public class PathwayControllerTest {

    @Mock
    PathwayService pathwayService;
    private ObjectMapper mapper = new ObjectMapper();
    @InjectMocks
    private PathwayController pathwayController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(pathwayController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }


    @Test
    public void testGetPathway() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/user/" + user.getId() + "/pathway/" +
                PathwayTypes.DONORPATHWAY.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(pathwayService, Mockito.times(1)).getPathway(user.getId(), PathwayTypes.DONORPATHWAY);
    }

    @Test
    public void testUpdatePathway() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Pathway pathway = new Pathway();
        pathway.setId(1L);
        pathway.setPathwayType(PathwayTypes.DONORPATHWAY);
        pathway.setStages(new HashMap<String, Stage>());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + user.getId() + "/pathway")
                .content(mapper.writeValueAsString(pathway))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(pathwayService, Mockito.times(1)).updatePathway(anyLong(), any(Pathway.class));
    }

    @Test
    public void testUpdatePathway_InvalidRole() throws Exception {
        User user = TestUtils.createUser("testuser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Pathway pathway = new Pathway();
        pathway.setId(1L);
        pathway.setPathwayType(PathwayTypes.DONORPATHWAY);
        pathway.setStages(new HashMap<String, Stage>());

        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + user.getId() + "/pathway")
                .content(mapper.writeValueAsString(pathway))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

}
