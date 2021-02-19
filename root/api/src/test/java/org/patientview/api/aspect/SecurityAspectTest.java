package org.patientview.api.aspect;

/**
 * Created by james@solidstategroup.com
 * Created on 27/07/2014
 */

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.api.service.GroupService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.test.util.TestUtils;
import org.patientview.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = false)
@ComponentScan(basePackages = {"org.patientview.api.aspect","org.patientview.api.service"})
public class SecurityAspectTest {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAspectTest.class);

    @Mock
    private GroupService groupService;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private SecurityAspect securityAspect = new SecurityAspect();


    @UserOnly
    public void testUserAnnotation(Long userId) {
        // Doesn't need content just test the annotation
    }

    @GroupMemberOnly(roles = {RoleName.UNIT_ADMIN, RoleName.SPECIALTY_ADMIN})
    public void testGroupAnnotation(Long groupId) {
        // Doesn't need content just test the annotation
    }

    @GroupMemberOnly(roles = {RoleName.UNIT_ADMIN, RoleName.SPECIALTY_ADMIN})
    public void testGroupAnnotation(Group group) {
        // Doesn't need content just test the annotation
    }

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

    }

    /**
     * Test: Test if the aspect is called when invoked a method with the annotation
     * Fail: The aspect is not invoked
     *
     * @throws Throwable
     */
    @Test(expected = ResourceForbiddenException.class)
    public void testGroupMemberOnly() throws Throwable {
        LOG.info("Security Aspect Test");
        Group testGroup = TestUtils.createGroup("testUser");
        testGroupAnnotation(testGroup.getId());

    }

    /**
     * Test: An authenticated thread and User gets passed for security validate via Group membership
     * Fail: An exception is thrown and the service methods are not called.
     * @throws Throwable
     */
    @Test
    public void testGroupMemberOnlyWithData_groupId() throws Throwable {

        User user = TestUtils.createUser( "testUser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);

        Set<GroupRole> roles = new HashSet<>();
        roles.add(groupRole);
        // Groups to be returned for the User search
        List<Group> groups = new ArrayList<>();
        groups.add(group);

        TestUtils.authenticateTest(user, roles);

        // Test the method
        testGroupAnnotation(group.getId());

        LOG.info("Executed security check successfully with out exception");

    }


    /**
     * Test: An authenticated thread and User gets passed for security validate via Group membership
     * Fail: An exception is thrown and the service methods are not called.
     * @throws Throwable
     */
    @Test
    public void testGroupMemberOnlyWithData_groupObject() throws Throwable {

        User user = TestUtils.createUser( "testUser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);

        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        // Groups to be returned for the User search
        List<Group> groups = new ArrayList<>();
        groups.add(group);

        TestUtils.authenticateTest(user, groupRoles);

        testGroupAnnotation(group);

        LOG.info("Executed security check successfully without exception");

    }


    /**
     * Test: An authenticated thread and User gets passed for security validate via Group membership
     * Fail: An exception is not thrown or the service methods are not called.
     * @throws Throwable
     */
    @Test(expected = ResourceForbiddenException.class)
    public void testGroupMemberOnlyWithData_UserIdFail() throws Throwable {

        User user = TestUtils.createUser( "testUser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);

        Collection<GroupRole> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(groupRole);

        TestUtils.authenticateTest(user, grantedAuthorities);

        testGroupAnnotation(TestUtils.createGroup("NewTestGroup").getId());

    }


    /**
     * Test: An authenticated thread and User gets passed for security validate via Group membership
     * Fail: An exception is not thrown or the service methods are not called.
     * @throws Throwable
     */
    @Test(expected = ResourceForbiddenException.class)
    public void testGroupMemberOnlyWithData_UserObjectFail() throws Throwable {

        User user = TestUtils.createUser("testUser");
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);

        Collection<GroupRole> grantedAuthorities = new HashSet<>();
        grantedAuthorities.add(groupRole);

        TestUtils.authenticateTest(user, grantedAuthorities);

        testGroupAnnotation(TestUtils.createGroup("NewTestGroup"));

    }

    /**
     * Test: An authenticate thread and User gets password to the security aspect
     * Fail: An exception is not raised
     */
    @Test
    public void testUserOnly() {
        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user);
        testUserAnnotation(user.getId());
    }


    /**
     * Test: An authenticate thread and User gets password to the security aspect
     * Fail: An exception is raised
     */
    @Test(expected = ResourceForbiddenException.class)
    public void testUserOnly_Fail() {
        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user);
        testUserAnnotation(1L);
    }
}
