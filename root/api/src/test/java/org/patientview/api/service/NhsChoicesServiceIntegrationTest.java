package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.patientview.api.config.TestCommonConfig;
import org.patientview.api.service.impl.PavNhsChoicesServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.test.util.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/01/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestCommonConfig.class, PavNhsChoicesServiceImpl.class})
public class NhsChoicesServiceIntegrationTest {

    User creator;

    @Autowired
    PavNhsChoicesServiceImpl pavNhsChoicesService;

//    @Autowired
//    CategoryRepository categoryRepository;

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
    //@Ignore("fails on build, test locally")
    public void testOrganisationsUpdate()
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        pavNhsChoicesService.updateOrganisations();
    }

    @Test
    //@Ignore("fails on build, test locally")
    public void testConditionsUpdate()
            throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        try {
            pavNhsChoicesService.updateConditions();
        } catch (Exception e) {
            System.out.println("Error updating from NHS Choices: " + e.getMessage());
        }
    }

    @Test
    @Ignore("fails on build, test locally")
    public void testGetDetailsByPracticeCode() {
//        Map<String, String> details = nhsChoicesService.getDetailsByPracticeCode("P91017");
//        Assert.assertNotNull(details);
//        Assert.assertNotNull(details.get("url"));
//        System.out.println(details.get("url"));
    }
}
