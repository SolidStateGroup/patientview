package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.SecurityAspect;
import org.patientview.api.controller.model.UnitRequest;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.AdminService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.GroupStatisticService;
import org.patientview.api.service.JoinRequestService;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.JoinRequest;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Location;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.model.enums.JoinRequestStatus;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.test.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
public class GroupControllerTest {

    protected final Logger LOG = LoggerFactory.getLogger(GroupControllerTest.class);

    private ObjectMapper mapper = new ObjectMapper();

    User creator;

    @Mock
    private AdminService adminService;

    @Mock
    private GroupService groupService;

    @Mock
    private JoinRequestService joinRequestService;

    @Mock
    private GroupStatisticService groupStatisticService;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private SecurityAspect securityAspect = SecurityAspect.aspectOf();

    @InjectMocks
    private GroupController groupController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        creator = TestUtils.createUser("creator");
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
    }

    @Test
    public void testGetGroupByType() {

        Long typeId = 9L;

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/group/type/" + typeId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).findGroupByType(eq(typeId));

    }

    @Test
    public void testAddChildGroup() {

        Long groupId = 1L;
        Long childGroupId = 2L;

        String url = "/group/" + groupId + "/child/" + childGroupId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).addChildGroup(eq(groupId), eq(childGroupId));
    }

    @Test
    public void testAddParentGroup() {

        Long groupId = 1L;
        Long parentGroupId = 2L;

        String url = "/group/" + groupId + "/parent/" + parentGroupId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).addParentGroup(eq(groupId), eq(parentGroupId));
    }

    @Test
    public void testAddFeature() {

        Long groupId = 1L;
        Long featureId = 2L;

        String url = "/group/" + groupId + "/features/" + featureId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.put(url)).andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).addFeature(eq(groupId), eq(featureId));
    }

    @Test
    public void testDeleteFeature() {

        Long groupId = 1L;
        Long featureId = 2L;

        String url = "/group/" + groupId + "/features/" + featureId;

        try {
            mockMvc.perform(MockMvcRequestBuilders.delete(url)).andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).deleteFeature(eq(groupId), eq(featureId));
    }


    /**
     * Test: The submission of a JoinRequest object to the controller
     * Fail: The JoinRequest object is not passed to the server
     */
    @Test
    public void testAddJoinRequest() {

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setId(1L);
        joinRequest.setForename("Test");
        joinRequest.setSurname("User");
        joinRequest.setDateOfBirth(new Date());
        joinRequest.setStatus(JoinRequestStatus.SUBMITTED);


        Long groupId = 2L;

        try {
            when(joinRequestService.add(eq(groupId), eq(joinRequest))).thenReturn(joinRequest);
            mockMvc.perform(MockMvcRequestBuilders.post("/group/" + groupId + "/joinRequest")
                    .content(mapper.writeValueAsString(joinRequest)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
            verify(joinRequestService, Mockito.times(1)).add(eq(groupId), eq(joinRequest));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }

    }

    /**
     * Test: The request of a parent group with the child
     * Fail: The 200 is not returned and the service is not called
     */
    @Test
    public void testGetChildGroups() throws ResourceNotFoundException {

        Long groupId = 9L;

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/group/" + groupId + "/children")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupService, Mockito.times(1)).findChildren(eq(groupId));

    }

    @Test
    public void testAddAdditionalLocation() {
        Location location = new Location();
        location.setId(1L);
        location.setLabel("Additional Location");
        location.setName("New location");
        location.setPhone("0123456789");
        location.setAddress("1 Road Street, Town, AB12CD");
        location.setWeb("http://www.additional.com");
        location.setEmail("test@solidstategroup.com");

        Long groupId = 2L;

        try {
            when(groupService.addLocation(eq(groupId), eq(location))).thenReturn(location);
            mockMvc.perform(MockMvcRequestBuilders.post("/group/" + groupId + "/locations")
                    .content(mapper.writeValueAsString(location)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
            verify(groupService, Mockito.times(1)).addLocation(eq(groupId), eq(location));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }
    }

    @Test
    public void testAddContactPoint() {
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setId(1L);

        ContactPointType contactPointType = new ContactPointType();
        contactPointType.setId(2L);
        contactPointType.setValue(ContactPointTypes.PV_ADMIN_NAME);
        contactPoint.setContactPointType(contactPointType);

        contactPoint.setContent("test@solidstategroup.com");

        Long groupId = 3L;

        try {
            when(groupService.addContactPoint(eq(groupId), eq(contactPoint))).thenReturn(contactPoint);
            mockMvc.perform(MockMvcRequestBuilders.post("/group/" + groupId + "/contactpoints")
                    .content(mapper.writeValueAsString(contactPoint)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
            verify(groupService, Mockito.times(1)).addContactPoint(eq(groupId), eq(contactPoint));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }
    }

    @Test
    public void testAddLink() {
        Link link = new Link();
        link.setId(1L);
        link.setDisplayOrder(1);
        link.setName("Home");
        link.setLink("http://www.solidstategroup.com");

        Long groupId = 2L;

        try {
            when(groupService.addLink(eq(groupId), eq(link))).thenReturn(link);
            mockMvc.perform(MockMvcRequestBuilders.post("/group/" + groupId + "/links")
                    .content(mapper.writeValueAsString(link)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isCreated());
            verify(groupService, Mockito.times(1)).addLink(eq(groupId), eq(link));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }
    }


    /**
     * Test: The retrieval of the group statistics for a specific group
     * Fail: The statistics service is not contacted about the request
     */
    @Test
    public void testGroupStatistics() throws ResourceNotFoundException {

        Group group = TestUtils.createGroup("testGroup");
        TestUtils.authenticateTest(TestUtils.createUser("testUser"));

        when(groupRepository.findOne(group.getId())).thenReturn(group);

        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/group/" + group.getId() + "/statistics"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            Assert.fail("Exception throw");
        }

        verify(groupStatisticService, Mockito.times(1)).getMonthlyGroupStatistics(eq(group.getId()));

    }

    /**
     * Test: The submission of a password reset object to the controller
     * Fail: The password reset object is not passed to the service
     */
    @Test
    public void testContactUnit() {

        UnitRequest unitRequest = new UnitRequest();
        unitRequest.setForename("Test");
        unitRequest.setSurname("User");
        unitRequest.setDateOfBirth(new Date());
        unitRequest.setNhsNumber("ASDASDA");
        Long groupId = 2L;

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/group/" + groupId + "/contactunit")
                    .content(mapper.writeValueAsString(unitRequest)).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            verify(groupService, Mockito.times(1)).contactUnit(eq(groupId), any(UnitRequest.class));
        } catch (Exception e) {
            Assert.fail("This call should not fail");
        }

    }

}


