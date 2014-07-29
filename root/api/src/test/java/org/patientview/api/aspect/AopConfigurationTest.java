package org.patientview.api.aspect;

/**
 * Created by james@solidstategroup.com
 * Created on 27/07/2014
 */

import org.aspectj.lang.JoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.GroupService;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.Roles;
import org.patientview.test.util.TestUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy(proxyTargetClass=false)
@ComponentScan(value={"org.patientview.api.aspect","org.patientview.api.service"})
public class AopConfigurationTest {


    User creator;

    @Mock
    GroupService groupService;

    @Mock
    JoinPoint joinPoint;

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
    @Test
    public void testGroupMemberOnly() throws Throwable {

        User testUser = TestUtils.createUser(1L, "testUser");
        Group testGroup = TestUtils.createGroup(2L, "testGroup", creator);
        Role testRole = TestUtils.createRole(3L, Roles.UNIT_ADMIN, creator);

        Set<Role> roles = new HashSet<Role>();
        roles.add(testRole);

        List<Group> groups = new ArrayList<Group>();
        groups.add(testGroup);

        TestUtils.authenticateTest(testUser, roles);


        try {
            groupService.findOne(1L);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
