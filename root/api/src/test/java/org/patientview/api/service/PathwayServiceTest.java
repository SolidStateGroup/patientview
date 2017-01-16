package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.PathwayServiceImpl;
import org.patientview.builder.PathwayBuilder;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Pathway;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.PathwayRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for Note service
 */
public class PathwayServiceTest {

    User creator;

    @Mock
    AuditRepository auditRepository;

    @Mock
    PathwayRepository pathwayRepository;

    @InjectMocks
    PathwayService pathwayService = new PathwayServiceImpl();

    @Mock
    Properties properties;

    @Mock
    RoleRepository roleRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }


    @Test
    public void testGetPathway() throws ResourceNotFoundException, ResourceForbiddenException {

        // user to get pathway for
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN_API, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Pathway pathway = PathwayBuilder.newBuilder()
                .setUser(patient)
                .setCreator(user)
                .setLastUpdater(user)
                .setType(PathwayTypes.DONORPATHWAY)
                .build();
        pathway.setId(1L);


        PathwayTypes pathwayType = PathwayTypes.DONORPATHWAY;
        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patient);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);
        when(pathwayRepository.findByUserAndPathwayType(eq(patient), eq(pathwayType))).thenReturn(pathway);

        org.patientview.api.model.Pathway pathwayApi = pathwayService.getPathway(patient.getId(), pathwayType);
        Assert.assertNotNull("Should have found pathway", pathwayApi);
        Assert.assertNotNull("Should have pathway type", pathwayApi.getPathwayType());
        Assert.assertNotNull("Should have note stages", pathwayApi.getStages());
    }

    @Test
    public void testUpdatePathway() throws ResourceNotFoundException, ResourceForbiddenException {

        // user to add pathway for
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN_API, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Pathway pathway = PathwayBuilder.newBuilder()
                .setUser(patient)
                .setCreator(user)
                .setLastUpdater(user)
                .setType(PathwayTypes.DONORPATHWAY)
                .build();
        pathway.setId(1L);

        org.patientview.api.model.Pathway pathwayApi = new org.patientview.api.model.Pathway(pathway);

        PathwayTypes pathwayType = PathwayTypes.DONORPATHWAY;
        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patient);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);
        when(pathwayRepository.findOne(eq(pathwayApi.getId()))).thenReturn(pathway);

        pathwayService.updatePathway(patient.getId(), pathwayApi);
        verify(pathwayRepository, Mockito.times(1)).save(any(Pathway.class));
    }

    @Test
    public void testSetupPathway() throws ResourceNotFoundException, ResourceForbiddenException {

        // user to add note for
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN_API, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patient);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);

        pathwayService.setupPathway(patient);
        verify(pathwayRepository, Mockito.times(1)).save(any(Pathway.class));
    }

    @Test
    public void testSetupPathway_alreadyExist() throws ResourceNotFoundException, ResourceForbiddenException {

        // user to add note for
        Group groupPatient = TestUtils.createGroup("GROUP1");
        Role rolePatient = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        GroupRole groupRolePatient = TestUtils.createGroupRole(rolePatient, groupPatient, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);

        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN_API, RoleType.STAFF);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Pathway pathway = PathwayBuilder.newBuilder()
                .setUser(patient)
                .setCreator(user)
                .setLastUpdater(user)
                .setType(PathwayTypes.DONORPATHWAY)
                .build();
        pathway.setId(1L);


        PathwayTypes pathwayType = PathwayTypes.DONORPATHWAY;

        when(userRepository.findOne(eq(patient.getId()))).thenReturn(patient);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);
        when(pathwayRepository.findByUserAndPathwayType(eq(patient), eq(pathwayType))).thenReturn(pathway);

        pathwayService.setupPathway(patient);
        verify(pathwayRepository, Mockito.times(0)).save(any(Pathway.class));
    }


}

