package org.patientview.api.aspect;

/**
 * Created by james@solidstategroup.com
 * Created on 27/07/2014
 */

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.config.TestServiceConfig;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.impl.GroupServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.Roles;
import org.patientview.test.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

@ContextConfiguration(classes={TestServiceConfig.class})
@WebAppConfiguration("src/main/java")
public class SecurityAspectTest {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityAspectTest.class);

    private User creator;

    @Mock
    JoinPoint joinPoint;

    @Mock
    private GroupService groupService;

    @Mock
    private JoinPoint.StaticPart staticPart;


    @Mock
    private MethodSignature methodSignature;

    @Mock
    private Signature signature;

   // @InjectMocks
   // private SecurityAspect securityAspect = new SecurityAspectImpl();


    @Before
    public void setUp() throws Exception {
        creator = TestUtils.createUser(1L, "creator");
        MockitoAnnotations.initMocks(this);

    }

    /**
     * Test: Test if the aspect is called when invoked a method with the annotation
     * Fail: The aspect is not invoked
     *
     * @throws Throwable
     */
    @Test(expected = SecurityException.class)
   // @Ignore
    public void testGroupMemberOnly() throws Throwable {

        User testUser = TestUtils.createUser(1L, "testUser");
        groupService.findOne(testUser.getId());

    }

    @Test
    public void testGroupMemberOnlyWithData() throws Throwable {

        User testUser = TestUtils.createUser(1L, "testUser");

        Group testGroup = TestUtils.createGroup(2L, "testGroup", creator);
        Role testRole = TestUtils.createRole(3L, Roles.UNIT_ADMIN, creator);


        Set<Role> roles = new HashSet<Role>();
        roles.add(testRole);

        List<Group> groups = new ArrayList<Group>();
        groups.add(testGroup);

        TestUtils.authenticateTest(testUser, roles);


        when(joinPoint.getStaticPart()).thenReturn(staticPart);
        when(staticPart.getSignature()).thenReturn(methodSignature);

        Class<GroupServiceImpl> groupServiceClass = GroupServiceImpl.class;
        Method method = getMethodSignature(groupServiceClass);

        when(methodSignature.getMethod()).thenReturn(method);

        //securityAspect.checkGroupMembership(joinPoint);

        LOG.info(method.getName());

    }

    private Method getMethodSignature(Class<GroupServiceImpl> groupServiceClass) {

        for (Method method : groupServiceClass.getDeclaredMethods()) {
            if (method.getName().equalsIgnoreCase("findOne")) {
                return method;
            }
        }

        return null;
    }


}
