package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.LocationService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Location;
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

import static org.junit.Assert.fail;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 04/08/2014
 */
public class LocationControllerTest {

    @InjectMocks
    private LocationController locationController;

    @Mock
    private LocationService locationService;

    private ObjectMapper mapper = new ObjectMapper();

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(locationController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testAddLocation() {
        Location location = new Location();
        location.setId(1L);
        location.setLabel("Additional Location");
        location.setName("New location");
        location.setPhone("0123456789");
        location.setAddress("1 Road Street, Town, AB12CD");
        location.setWeb("http://www.additional.com");
        location.setEmail("test@solidstategroup.com");

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/group/" + group.getId() + "/locations")
                    .content(mapper.writeValueAsString(location)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test(expected = AssertionError.class)
    public void testAddLocationWrongGroup() throws Exception {
        Location location = new Location();
        location.setId(1L);
        location.setLabel("Additional Location");
        location.setName("New location");
        location.setPhone("0123456789");
        location.setAddress("1 Road Street, Town, AB12CD");
        location.setWeb("http://www.additional.com");
        location.setEmail("test@solidstategroup.com");

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Group group2 = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group2, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.post("/group/" + group.getId() + "/locations")
                .content(mapper.writeValueAsString(location)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    public void testUpdateLocation() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        Location testLocation = new Location();
        testLocation.setId(1L);

        try {
            mockMvc.perform(MockMvcRequestBuilders.put("/location")
                    .content(mapper.writeValueAsString(testLocation)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("This call should not fail");
        }
    }

    @Test
    public void testDeleteLocation() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        Long locationId = 1L;
        String url = "/location/" + locationId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception throw");
        }
    }
}
