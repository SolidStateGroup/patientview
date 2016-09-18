package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.GroupStatisticTO;
import org.patientview.api.model.NhsIndicators;
import org.patientview.api.service.impl.GroupStatisticsServiceImpl;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupStatisticRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.test.util.TestUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 11/08/2014
 */
public class GroupStatisticsServiceTest {

    User creator;

    @Mock
    CodeRepository codeRepository;

    @Mock
    EntityManager entityManager;

    @Mock
    FhirLinkRepository fhirLinkRepository;

    @Mock
    FhirResource fhirResource;

    @Mock
    GroupRepository groupRepository;

    @Mock
    LookupRepository lookupRepository;

    @Mock
    LookupTypeRepository lookupTypeRepository;

    @Mock
    GroupStatisticRepository groupStatisticRepository;

    @InjectMocks
    GroupStatisticService groupStatisticService = new GroupStatisticsServiceImpl();

    @Mock
    Query query;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: The retrieval of monthly statistics for groups
     * Fail: The repository is not accessed to retrieve the results
     */
    @Test
    public void testGetMonthlyGroupStatistics() throws Exception {
        Group group = TestUtils.createGroup("testGroup");

        // user and security
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);

        List<GroupStatisticTO> groupStatisticTOs = groupStatisticService.getMonthlyGroupStatistics(group.getId());

        verify(groupStatisticRepository, Mockito.times(1)).findByGroupAndStatisticPeriod(eq(group),
                eq(StatisticPeriod.MONTH));
    }

    @Test
    public void testGetNhsIndicators() throws Exception {
        Group group = TestUtils.createGroup("testGroup");

        // user and security
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.add(new FhirLink(1L, UUID.randomUUID(), new User()));

        List<UUID> uuids = new ArrayList<>();
        uuids.add(fhirLinks.get(0).getResourceId());

        Code code = new Code();
        code.setCode("TP");
        List<Code> foundCodes = new ArrayList<>(Arrays.asList(code));

        List<String> codeStrings = new ArrayList<>();
        codeStrings.add(code.getCode());

        Long zeroZeroCount = 20L;

        List<Group> groups = new ArrayList<>();
        groups.add(group);

        when(codeRepository.findAllByCodes(any(List.class))).thenReturn(foundCodes);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(fhirLinkRepository.findByGroups(eq(groups))).thenReturn(fhirLinks);
        when(fhirLinkRepository.findByGroupsAndRecentLogin(eq(groups), any(Date.class))).thenReturn(fhirLinks);
        when(fhirResource.getCountEncounterBySubjectIdsAndCodes(eq(uuids), any(List.class))).thenReturn(zeroZeroCount);

        NhsIndicators nhsIndicators = groupStatisticService.getNhsIndicators(group.getId());

        assertEquals("Should have correct Group ID", group.getId(), nhsIndicators.getGroupId());
        assertTrue("Should have at least one Code in codeMap",
                nhsIndicators.getData().getIndicatorCodeMap().get("Transplant").size() > 0);
        assertEquals("Should have correct Code in codeMap",
                code.getCode(), nhsIndicators.getData().getIndicatorCodeMap().get("Transplant").get(0));
        assertEquals("Should have correct count for Code in codeCount",
                zeroZeroCount, nhsIndicators.getData().getIndicatorCount().get("Transplant"));

        verify(codeRepository, Mockito.atLeastOnce()).findAllByCodes(any(List.class));
        verify(groupRepository, Mockito.times(1)).findOne(eq(group.getId()));
        verify(fhirLinkRepository, Mockito.times(1)).findByGroups(eq(groups));
        verify(fhirLinkRepository, Mockito.times(1)).findByGroupsAndRecentLogin(eq(groups), any(Date.class));
        verify(fhirResource, Mockito.times(8)).getCountEncounterBySubjectIdsAndCodes(eq(uuids), any(List.class));
        verify(fhirResource, Mockito.times(2)).getCountEncounterBySubjectIdsAndNotCodes(eq(uuids), any(List.class));
    }

    /**
     * Test: The generation of monthly statistics for all groups
     * Fail: The statistics will not be generated for any groups
     */
    @Test
    public void testGenerateGroupStatistics() {
        // Create statistical lookups
        Set<Lookup> lookups = new HashSet<>();
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.STATISTIC_TYPE);
        lookupType.setLookups(lookups);
        lookupType.getLookups().add(TestUtils.createLookup(lookupType, "TestStatistics1", "SOME SQL"));
        lookupType.getLookups().add(TestUtils.createLookup(lookupType, "TestStatistics2", "SOME SQL"));

        // Create dates
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        // Get to the first of the month for a starting point
        calendar.roll(Calendar.DAY_OF_MONTH, -calendar.get(Calendar.DAY_OF_MONTH) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();

        // Create groups
        Group testGroup = TestUtils.createGroup( "testGroup");
        List<Group> groups = new ArrayList<>();
        groups.add(testGroup);
        when(groupRepository.findAll()).thenReturn(groups);


        when(lookupTypeRepository.findByType(eq(LookupTypes.STATISTIC_TYPE))).thenReturn(lookupType);
        when(entityManager.createNativeQuery(any(String.class))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(BigInteger.ONE);
        groupStatisticService.generateGroupStatistic(startDate, endDate, StatisticPeriod.MONTH);

        // There should be 2 results returned cannot do eq on a date with a timestamp
     //   verify(query, Mockito.times(2)).setParameter(eq("startDate"), eq(startDate));
     //   verify(query, Mockito.times(2)).setParameter(eq("endDate"), eq(endDate));

        verify(query, Mockito.times(2)).setParameter(eq("groupId"), eq(testGroup.getId()));
        verify(query, Mockito.times(2)).getSingleResult();
        verify(groupStatisticRepository, Mockito.times(2)).save(any(GroupStatistic.class));

    }

    /**
     * Test: The retrieval of monthly statistics for groups with an invalid group
     * Fail: The exception is not thrown
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testGetMonthlyGroupStatistics_UnknownGroup() throws Exception {
        Group group = TestUtils.createGroup("testGroup");

        // user and security
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);

        groupStatisticService.getMonthlyGroupStatistics(null);
    }
}
