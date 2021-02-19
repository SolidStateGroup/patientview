package org.patientview.api.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.aspect.SecurityAspect;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.GroupStatisticService;
import org.patientview.api.service.RequestService;
import org.patientview.api.service.Timer;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/07/2017
 */
public class GroupStatisticsControllerTest {

    @Mock
    private GroupStatisticService groupStatisticService;

    @Mock
    private Timer timer;

    @InjectMocks
    private SecurityAspect securityAspect = new SecurityAspect();

    @InjectMocks
    private GroupStatisticsController groupStatisticsController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(groupStatisticsController).build();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGenerateMonthlyStatistics() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        when(timer.getCurrentDate()).thenReturn(new GregorianCalendar());

        mockMvc.perform(MockMvcRequestBuilders.get("/admin/group/generatemonthlystatistics"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(groupStatisticService, times(1))
                .generateGroupStatisticAdminOnly(any(Date.class), any(Date.class), eq(StatisticPeriod.MONTH));
    }

    @Test
    public void testGroupStatistics() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        mockMvc.perform(MockMvcRequestBuilders.get("/group/" + group.getId() + "/statistics"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}


