package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.ObservationHeadingServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.ResultCluster;
import org.patientview.persistence.model.ResultClusterObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.ObservationHeadingGroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.ResultClusterRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityExistsException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
public class ObservationHeadingServiceTest {

    @Mock
    AlertRepository alertRepository;

    @Mock
    ApiObservationService apiObservationService;

    @Mock
    EmailService emailService;

    @Mock
    GroupRepository groupRepository;

    @Mock
    ObservationHeadingGroupRepository observationHeadingGroupRepository;

    @Mock
    ObservationHeadingRepository observationHeadingRepository;

    @InjectMocks
    ObservationHeadingService observationHeadingService = new ObservationHeadingServiceImpl();

    @Mock
    Properties properties;

    @Mock
    ResultClusterRepository resultClusterRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    UserRepository userRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: To see if the observation headings are returned
     * Fail: The calls to the repository are not made, not the right number
     */
    @Test
    public void testFindAll() {
        Pageable pageableAll = PageRequest.of(0, Integer.MAX_VALUE);
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);

        List<ObservationHeading> observationHeadings = new ArrayList<>();
        ObservationHeading observationHeading1 = new ObservationHeading();
        observationHeading1.setCode("OBS1");
        observationHeadings.add(observationHeading1);
        ObservationHeading observationHeading2 = new ObservationHeading();
        observationHeading2.setCode("OBS2");
        observationHeadings.add(observationHeading2);

        Page<ObservationHeading> observationHeadingsPage =
                new PageImpl<>(observationHeadings, pageableAll, observationHeadings.size());

        when(observationHeadingRepository.findAllMinimal(eq(pageableAll))).thenReturn(observationHeadingsPage);

        Page<ObservationHeading> result = observationHeadingService.findAll(new GetParameters());
        Assert.assertEquals("Should have 2 observation headings", 2, result.getNumberOfElements());
        verify(observationHeadingRepository, Mockito.times(1)).findAllMinimal(Matchers.eq(pageableAll));
    }

    @Test
    public void testAdd() throws ResourceInvalidException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        when(observationHeadingRepository.save(eq(observationHeading))).thenReturn(observationHeading);
        ObservationHeading savedObservationHeading = observationHeadingService.add(observationHeading);

        Assert.assertNotNull("The returned observation heading should not be null", savedObservationHeading);
        verify(observationHeadingRepository, Mockito.times(1)).save(eq(savedObservationHeading));
    }

    @Test(expected = EntityExistsException.class)
    public void testAddDuplicate() throws ResourceInvalidException {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        List<ObservationHeading> observationHeadings = new ArrayList<>();
        observationHeadings.add(observationHeading);
        when(observationHeadingRepository.save(eq(observationHeading))).thenReturn(observationHeading);
        when(observationHeadingRepository.findByCode(eq(observationHeading.getCode()))).thenReturn(observationHeadings);

        ObservationHeading savedObservationHeading = observationHeadingService.add(observationHeading);

        Assert.assertNotNull("The returned observation heading should not be null", savedObservationHeading);
        verify(observationHeadingRepository, Mockito.times(1)).save(eq(savedObservationHeading));

        ObservationHeading observationHeading2 = TestUtils.createObservationHeading("OBS1");
        observationHeadingService.add(observationHeading2);
    }

    @Test
    public void testSave() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);

        when(observationHeadingRepository.save(eq(observationHeading))).thenReturn(observationHeading);
        when(observationHeadingRepository.findById(eq(observationHeading.getId())))
                .thenReturn(Optional.of(observationHeading));

        try {
            observationHeadingService.save(observationHeading);
        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException thrown");
        }

        verify(observationHeadingRepository, Mockito.times(1)).save(eq(observationHeading));
    }

    @Test
    public void testAddGroup() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Group group2 = TestUtils.createGroup("testGroup2");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        when(observationHeadingRepository.findById(eq(observationHeading.getId())))
                .thenReturn(Optional.of(observationHeading));
        when(observationHeadingRepository.save(eq(observationHeading))).thenReturn(observationHeading);
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRepository.findById(eq(group2.getId()))).thenReturn(Optional.of(group2));

        try {
            observationHeadingService.addObservationHeadingGroup(observationHeading.getId(), group.getId(), 3L, 4L);
        } catch (ResourceNotFoundException | ResourceForbiddenException e) {
            Assert.fail("Exception: " + e.getMessage());
        }

        verify(observationHeadingRepository, Mockito.times(1)).save(eq(observationHeading));
    }

    @Test
    public void testUpdateGroup() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        Group group = TestUtils.createGroup("GROUP1");

        // user and security
        Group group2 = TestUtils.createGroup("GROUP2");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group2, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        ObservationHeadingGroup observationHeadingGroup
                = new ObservationHeadingGroup(observationHeading, group, 3L, 4L);
        observationHeadingGroup.setId(1L);

        // update from group1 to group2 by specialty admin in group 2
        org.patientview.api.model.ObservationHeadingGroup apiObservationHeadingGroup
                = new org.patientview.api.model.ObservationHeadingGroup(
                    new ObservationHeadingGroup(observationHeading, group2, 3L, 4L));

        apiObservationHeadingGroup.setId(observationHeadingGroup.getId());
        observationHeading.getObservationHeadingGroups().add(observationHeadingGroup);

        when(observationHeadingRepository.findById(eq(observationHeading.getId())))
                .thenReturn(Optional.of(observationHeading));
        when(observationHeadingRepository.save(eq(observationHeading))).thenReturn(observationHeading);
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(groupRepository.findById(eq(group2.getId()))).thenReturn(Optional.of(group2));

        try {
            observationHeadingService.updateObservationHeadingGroup(apiObservationHeadingGroup);
        } catch (ResourceNotFoundException | ResourceForbiddenException e) {
            Assert.fail("Exception: " + e.getMessage());
        }

        verify(observationHeadingRepository, Mockito.times(1)).save(eq(observationHeading));
    }

    @Test
    public void testRemoveGroup() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);
        Group group = TestUtils.createGroup("GROUP1");
        group.setId(2L);

        // specialty admin can only delete from own group
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        ObservationHeadingGroup observationHeadingGroup
                = new ObservationHeadingGroup(observationHeading, group, 3L, 4L);

        observationHeadingGroup.setId(3L);
        observationHeading.getObservationHeadingGroups().add(observationHeadingGroup);

        when(observationHeadingRepository.findById(eq(observationHeading.getId()))).
                thenReturn(Optional.of(observationHeading));
        when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);
        when(observationHeadingGroupRepository.findById(eq(observationHeadingGroup.getId())))
                .thenReturn(Optional.of(observationHeadingGroup));
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));

        try {
            observationHeadingService.removeObservationHeadingGroup(3L);
        } catch (ResourceNotFoundException | ResourceForbiddenException e) {
            Assert.fail("Exception: " + e.getMessage());
        }

        verify(observationHeadingGroupRepository, Mockito.times(1)).delete(eq(observationHeadingGroup));
    }

    @Test
    public void testGetResultClusters() {

        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.PATIENT);

        ObservationHeading observationHeading1 = new ObservationHeading();
        observationHeading1.setCode("OBS1");
        ObservationHeading observationHeading2 = new ObservationHeading();
        observationHeading2.setCode("OBS2");

        List<ResultCluster> resultClusters = new ArrayList<>();

        ResultCluster resultCluster = new ResultCluster();
        resultCluster.setName("TEST_RESULT_CLUSTER");

        List<ResultClusterObservationHeading> resultClusterObservationHeadings = new ArrayList<>();

        ResultClusterObservationHeading resultClusterObservationHeading = new ResultClusterObservationHeading();
        resultClusterObservationHeading.setObservationHeading(observationHeading1);
        resultClusterObservationHeading.setResultCluster(resultCluster);
        resultClusterObservationHeadings.add(resultClusterObservationHeading);

        ResultClusterObservationHeading resultClusterObservationHeading2 = new ResultClusterObservationHeading();
        resultClusterObservationHeading2.setObservationHeading(observationHeading2);
        resultClusterObservationHeading2.setResultCluster(resultCluster);
        resultClusterObservationHeadings.add(resultClusterObservationHeading2);

        resultCluster.setResultClusterObservationHeadings(resultClusterObservationHeadings);
        resultClusters.add(resultCluster);

        when(resultClusterRepository.findAll()).thenReturn(resultClusters);

        List<ResultCluster> results = observationHeadingService.getResultClusters();

        Assert.assertEquals("Should have 1 result cluster", 1, results.size());
        Assert.assertEquals("Should have 2 observation headings in result cluster", 2,
                results.get(0).getResultClusterObservationHeadings().size());

        verify(resultClusterRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void testGetAvailableAlertObservationHeadings() throws ResourceNotFoundException {

        Group group = TestUtils.createGroup("GROUP1");
        List<Group> groupList = new ArrayList<>();
        groupList.add(group);

        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setObservationHeadingGroups(new HashSet<ObservationHeadingGroup>());
        ObservationHeadingGroup observationHeadingGroup
                = new ObservationHeadingGroup(observationHeading, group, 1L, 1L);
        observationHeading.getObservationHeadingGroups().add(observationHeadingGroup);
        List<ObservationHeading> observationHeadings = new ArrayList<>();
        observationHeadings.add(observationHeading);

        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(groupRepository.findGroupByUser(eq(user))).thenReturn(groupList);
        when(observationHeadingRepository.findAll()).thenReturn(observationHeadings);

        List<ObservationHeading> result
                = observationHeadingService.getAvailableAlertObservationHeadings(user.getId());
        Assert.assertEquals("Should have 1 observation heading", 1, result.size());
    }
}

