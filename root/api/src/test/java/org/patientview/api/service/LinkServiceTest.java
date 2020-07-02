package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.LinkServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Link;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LinkRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.test.util.TestUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 15/07/2014
 */
public class LinkServiceTest {

    User creator;

    @Mock
    LinkRepository linkRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    CodeRepository codeRepository;

    @Mock
    LookupRepository lookupRepository;

    @InjectMocks
    LinkService linkService = new LinkServiceImpl();

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
    public void testCreateGroupLink() {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Link link = TestUtils.createLink(group, null, "groupLink");

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(linkRepository.save(eq(link))).thenReturn(link);

        try {
            linkService.addGroupLink(group.getId(), link);
        } catch (ResourceNotFoundException | ResourceForbiddenException e) {
            fail("Exception: " + e.getMessage());
        }

        Assert.assertNotNull("The returned link should not be null", link);
        verify(linkRepository, Mockito.times(1)).save(eq(link));
    }

    @Test(expected= ResourceForbiddenException.class)
    public void testCreateGroupLinkWrongGroup()
            throws ResourceNotFoundException, ResourceForbiddenException{

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Group group2 = TestUtils.createGroup("testGroup2");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group2, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Link link = TestUtils.createLink(group, null, "groupLink");

        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(linkRepository.save(eq(link))).thenReturn(link);

        linkService.addGroupLink(group.getId(), link);

        Assert.assertNotNull("The returned link should not be null", link);
        verify(linkRepository, Mockito.times(1)).save(eq(link));
    }

    @Test
    public void testCreateCodeLink() {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Code code = TestUtils.createCode("testCode");
        Link link = TestUtils.createLink(null, code, "groupLink");

        when(codeRepository.findById(eq(code.getId()))).thenReturn(Optional.of(code));
        when(linkRepository.save(eq(link))).thenReturn(link);

        try {
            linkService.addCodeLink(code.getId(), link);
        } catch (ResourceNotFoundException e) {
            fail("Exception: " + e.getMessage());
        }

        Assert.assertNotNull("The returned link should not be null", link);
        verify(linkRepository, Mockito.times(1)).save(eq(link));
    }

}
