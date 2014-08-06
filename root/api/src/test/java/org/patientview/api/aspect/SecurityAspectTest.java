package org.patientview.api.aspect;

/**
 * Created by james@solidstategroup.com
 * Created on 27/07/2014
 */

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.exception.ResourceForbiddenException;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.impl.GroupServiceImpl;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.Roles;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.test.util.TestUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = false)
@ComponentScan(basePackages = {"org.patientview.api.aspect","org.patientview.api.service"})
public class SecurityAspectTest {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAspectTest.class);

    private User creator;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private GroupService groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private JoinPoint.StaticPart staticPart;

    @Mock
    private MethodSignature methodSignature;

    @InjectMocks
    private SecurityAspect securityAspect;

    @Mock
    private Signature signature;



    @Before
    public void setUp() throws Exception {
        creator = TestUtils.createUser(1L, "creator");
        PowerMockito.mockStatic(Util.class);
        MockitoAnnotations.initMocks(this);

    }

    /**
     * Test: Test if the aspect is called when invoked a method with the annotation
     * Fail: The aspect is not invoked
     *
     * @throws Throwable
     */
    @Ignore
    @Test(expected = SecurityException.class)
    public void testGroupMemberOnly() throws Throwable {
        LOG.info("Security Aspect Test");
        Group testGroup = TestUtils.createGroup(1L, "testUser", creator);
        groupService.get(testGroup.getId());

    }

    /**
     * Test: A authenticate thread and User gets passed for security validate via Group membership
     * Fail: An exception is thrown and the service methods are not called.
     * @throws Throwable
     */
    @Test
    public void testGroupMemberOnlyWithData_UserId() throws Throwable {

        // User the is being authenticated
        User testUser = TestUtils.createUser(1L, "testUser");
        // Group for the user to access but also be a member of
        Group testGroup = TestUtils.createGroup(2L, "testGroup", creator);
        Role testRole = TestUtils.createRole(3L, Roles.UNIT_ADMIN, creator);
        // Roles for the user to be authenticated against
        Set<Role> roles = new HashSet<Role>();
        roles.add(testRole);
        // Groups to be returned for the User search
        List<Group> groups = new ArrayList<Group>();
        groups.add(testGroup);

        TestUtils.authenticateTest(testUser, roles);

        // Set up the joinpoint to return the correct parts of the method and group id
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[] {testGroup.getId()});

        // Get the correct annotation from example GroupServive
        Class<GroupServiceImpl> groupServiceClass = GroupServiceImpl.class;
        Method method = getMethodSignature(groupServiceClass);

        when(methodSignature.getMethod()).thenReturn(method);
        when(groupRepository.findOne(eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupService.findGroupByUser(any(User.class))).thenReturn(groups);
        Roles[] rolesNames = new Roles[] {Roles.STAFF_ADMIN, Roles.UNIT_ADMIN};
        when(Util.getRoles(joinPoint)).thenReturn(rolesNames);

        // FIX ME groupService.findGroupByUser does not seem to want to return this
        when(Util.iterableToList(groups)).thenReturn(groups);

        // Test the method
        securityAspect.checkGroupMembership(joinPoint);

        verify(groupRepository, Mockito.times(1)).findOne(eq(testGroup.getId()));
        verify(groupService, Mockito.times(1)).findGroupByUser(eq(testUser));

        LOG.info("Executed security check successfully");

    }


    /**
     * Test: A authenticate thread and User gets passed for security validate via Group membership
     * Fail: An exception is thrown and the service methods are not called.
     * @throws Throwable
     */
    @Test
    public void testGroupMemberOnlyWithData_UserObject() throws Throwable {

        // User the is being authenticated
        User testUser = TestUtils.createUser(1L, "testUser");
        // Group for the user to access but also be a member of
        Group testGroup = TestUtils.createGroup(2L, "testGroup", creator);
        Role testRole = TestUtils.createRole(3L, Roles.UNIT_ADMIN, creator);
        // Roles for the user to be authenticated against
        Set<Role> roles = new HashSet<Role>();
        roles.add(testRole);
        // Groups to be returned for the User search
        List<Group> groups = new ArrayList<Group>();
        groups.add(testGroup);

        TestUtils.authenticateTest(testUser, roles);

        // Set up the joinpoint to return the correct parts of the method and group id
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[] {testGroup});

        // Get the correct annotation from example GroupService
        Class<GroupServiceImpl> groupServiceClass = GroupServiceImpl.class;
        Method method = getMethodSignature(groupServiceClass);

        when(methodSignature.getMethod()).thenReturn(method);
        when(groupRepository.findOne(eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupService.findGroupByUser(any(User.class))).thenReturn(groups);
        Roles[] rolesNames = new Roles[] {Roles.STAFF_ADMIN, Roles.UNIT_ADMIN};
        when(Util.getRoles(joinPoint)).thenReturn(rolesNames);

        // FIX ME groupService.findGroupByUser does not seem to want to return this
        when(Util.iterableToList(groups)).thenReturn(groups);

        // Test the method
        securityAspect.checkGroupMembership(joinPoint);

        verify(groupRepository, Mockito.times(0)).findOne(eq(testGroup.getId()));
        verify(groupService, Mockito.times(1)).findGroupByUser(eq(testUser));

        LOG.info("Executed security check successfully");

    }


    /**
     * Test: A authenticate thread and User gets passed for security validate via Group membership
     * Fail: An exception is not thrown or the service methods are not called.
     * @throws Throwable
     */
    @Test(expected = ResourceForbiddenException.class)
    public void testGroupMemberOnlyWithData_UserIdFail() throws Throwable {

        // User the is being authenticated
        User testUser = TestUtils.createUser(1L, "testUser");
        // Group for the user to access but also be a member of
        Group testGroup = TestUtils.createGroup(2L, "testGroup", creator);
        Group otherTestGroup = TestUtils.createGroup(3L, "testOtherGroup", creator);
        Role testRole = TestUtils.createRole(3L, Roles.UNIT_ADMIN, creator);
        // Roles for the user to be authenticated against
        Set<Role> roles = new HashSet<Role>();
        roles.add(testRole);
        // Groups to be returned for the User search
        List<Group> groups = new ArrayList<Group>();
        groups.add(otherTestGroup);

        TestUtils.authenticateTest(testUser, roles);

        // Set up the joinpoint to return the correct parts of the method and group id
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[] {testGroup.getId()});

        // Get the correct annotation from example GroupServive
        Class<GroupServiceImpl> groupServiceClass = GroupServiceImpl.class;
        Method method = getMethodSignature(groupServiceClass);

        when(methodSignature.getMethod()).thenReturn(method);
        when(groupRepository.findOne(eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupService.findGroupByUser(any(User.class))).thenReturn(groups);
        Roles[] rolesNames = new Roles[] {Roles.STAFF_ADMIN, Roles.UNIT_ADMIN};
        when(Util.getRoles(joinPoint)).thenReturn(rolesNames);

        // FIX ME groupService.findGroupByUser does not seem to want to return this
        when(Util.iterableToList(groups)).thenReturn(groups);

        // Test the method
        securityAspect.checkGroupMembership(joinPoint);

        verify(groupRepository, Mockito.times(1)).findOne(eq(testGroup.getId()));
        verify(groupService, Mockito.times(1)).findGroupByUser(eq(testUser));

        LOG.info("Executed security check successfully");

    }


    /**
     * Test: A authenticate thread and User gets passed for security validate via Group membership
     * Fail: An exception is not thrown or the service methods are not called.
     * @throws Throwable
     */
    @Test(expected = ResourceForbiddenException.class)
    public void testGroupMemberOnlyWithData_UserObjectFail() throws Throwable {

        // User the is being authenticated
        User testUser = TestUtils.createUser(1L, "testUser");
        // Group for the user to access but also be a member of
        Group testGroup = TestUtils.createGroup(2L, "testGroup", creator);
        Group otherTestGroup = TestUtils.createGroup(3L, "testOtherGroup", creator);
        Role testRole = TestUtils.createRole(3L, Roles.UNIT_ADMIN, creator);
        // Roles for the user to be authenticated against
        Set<Role> roles = new HashSet<Role>();
        roles.add(testRole);
        // Groups to be returned for the User search
        List<Group> groups = new ArrayList<Group>();
        groups.add(otherTestGroup);

        TestUtils.authenticateTest(testUser, roles);

        // Set up the joinpoint to return the correct parts of the method and group id
        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(methodSignature);
        when(joinPoint.getArgs()).thenReturn(new Object[] {testGroup});

        // Get the correct annotation from example GroupServive
        Class<GroupServiceImpl> groupServiceClass = GroupServiceImpl.class;
        Method method = getMethodSignature(groupServiceClass);

        when(methodSignature.getMethod()).thenReturn(method);
        when(groupRepository.findOne(eq(testGroup.getId()))).thenReturn(testGroup);
        when(groupService.findGroupByUser(any(User.class))).thenReturn(groups);
        Roles[] rolesNames = new Roles[] {Roles.STAFF_ADMIN, Roles.UNIT_ADMIN};
        when(Util.getRoles(joinPoint)).thenReturn(rolesNames);

        // FIX ME groupService.findGroupByUser does not seem to want to return this
        when(Util.iterableToList(groups)).thenReturn(groups);

        // Test the method
        securityAspect.checkGroupMembership(joinPoint);

        verify(groupRepository, Mockito.times(1)).findOne(eq(testGroup.getId()));
        verify(groupService, Mockito.times(1)).findGroupByUser(eq(testUser));

        LOG.info("Executed security check successfully");

    }

    // Get a method that has the target annotation attached.
    private Method getMethodSignature(Class<GroupServiceImpl> groupServiceClass) {

        for (Method method : groupServiceClass.getDeclaredMethods()) {
            if (method.getName().equalsIgnoreCase("findOne")) {
                return method;
            }
        }

        return null;
    }


}
