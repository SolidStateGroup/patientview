package org.patientview.api.service;

import net.lingala.zip4j.exception.ZipException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.GpServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GpMasterRepository;
import org.patientview.test.util.TestUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 02/02/2016
 */
public class GpServiceTest {

    User creator;

    @Mock
    Properties properties;

    @Mock
    GpMasterRepository gpMasterRepository;

    @InjectMocks
    GpService gpService = new GpServiceImpl();

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

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
    @Ignore("fails on jenkins")
    public void testUpdateMasterTable() throws IOException, ZipException {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        when(properties.getProperty("gp.master.temp.directory")).thenReturn(testFolder.getRoot().getAbsolutePath());
        when(properties.getProperty("gp.master.url.england"))
                .thenReturn("file://" + getClass().getResource("/gp").getPath().concat("/epraccur.zip"));
        when(properties.getProperty("gp.master.filename.england")).thenReturn("epraccur.csv");
        gpService.updateMasterTable();
    }
}
