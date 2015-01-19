package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.ContactPointService;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
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
 * Created by james@solidstategroup.com
 * Created on 31/07/2014
 */
public class ContactPointControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ContactPointService contactPointService;

    @InjectMocks
    private ContactPointController contactPointController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(contactPointController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: Simple request to the contact type for a contact point
     * Fail: Doesn't return OK.
     */
    @Test
    @Ignore("Only used for migration")
    public void testGetContactPointType() throws ResourceInvalidException {
        String type = "testType";

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/contactpoint/type/" + type)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateContactPoint() {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setId(1L);
        contactPoint.setGroup(group);

        try {
            mockMvc.perform(MockMvcRequestBuilders.put("/contactpoint")
                    .content(mapper.writeValueAsString(contactPoint)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testDeleteContactPoint() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.UNIT_ADMIN);
        Long contactPointId = 1L;
        String url = "/contactpoint/" + contactPointId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testAddContactPoint() {
        ContactPoint contactPoint = new ContactPoint();
        ContactPointType contactPointType = new ContactPointType();
        contactPointType.setValue(ContactPointTypes.PV_ADMIN_NAME);
        contactPoint.setContactPointType(contactPointType);
        contactPoint.setContent("test@solidstategroup.com");

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/group/" + group.getId() + "/contactpoints")
                    .content(mapper.writeValueAsString(contactPoint)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test(expected = AssertionError.class)
    public void testAddContactPointWrongGroup() throws Exception {
        ContactPoint contactPoint = new ContactPoint();
        ContactPointType contactPointType = new ContactPointType();
        contactPointType.setValue(ContactPointTypes.PV_ADMIN_NAME);
        contactPoint.setContactPointType(contactPointType);
        contactPoint.setContent("test@solidstategroup.com");

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Group group2 = TestUtils.createGroup("testGroup2");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group2, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.post("/group/" + group.getId() + "/contactpoints")
                .content(mapper.writeValueAsString(contactPoint)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }
}
