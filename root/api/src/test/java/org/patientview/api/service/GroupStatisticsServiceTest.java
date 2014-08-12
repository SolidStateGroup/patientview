package org.patientview.api.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.GroupStatisticsServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupStatisticRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.test.util.TestUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    GroupStatisticRepository groupStatisticRepository;

    @Mock
    GroupRepository groupRepository;

    @Mock
    LookupRepository lookupRepository;

    @Mock
    LookupTypeRepository lookupTypeRepository;

    @Mock
    EntityManager entityManager;

    @Mock
    Query query;

    @InjectMocks
    GroupStatisticService groupStatisticService = new GroupStatisticsServiceImpl();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser(1L, "creator");
    }

    /**
     * Test: The retrieval of monthly statistics for groups
     * Fail: The repository is not accessed to retrieve the results
     */
    @Test
    public void testGetMonthlyGroupStatistics() throws ResourceNotFoundException {
        Group testGroup = TestUtils.createGroup(1L, "testGroup", creator);

        when(groupRepository.findOne(eq(testGroup.getId()))).thenReturn(testGroup);

        groupStatisticService.getMonthlyGroupStatistics(testGroup.getId());

        verify(groupStatisticRepository, Mockito.times(1)).findByGroupAndStatisticPeriod(eq(testGroup), eq(StatisticPeriod.MONTH));

    }

    /**
     * Test: The generation of monthly statistics for all groups
     * Fail: The statistics will not be generated for any groups
     */
    @Test
    public void testGenerateGroupStatistics() {
        // Create statistical lookups
        Set<Lookup> lookups = new HashSet<>();
        LookupType lookupType = TestUtils.createLookupType(2L, LookupTypes.STATISTIC_TYPE, creator);
        lookupType.setLookups(lookups);
        lookupType.getLookups().add(TestUtils.createLookup(3L, lookupType, "TestStatistics1", creator));
        lookupType.getLookups().add(TestUtils.createLookup(4L, lookupType, "TestStatistics2", creator));

        // Create dates
        Calendar calendar = Calendar.getInstance();
        Date startDate = calendar.getTime();
        calendar.roll(Calendar.MONTH, + 1);
        Date endDate = calendar.getTime();

        // Create groups
        Group testGroup = TestUtils.createGroup(1L, "testGroup", creator);
        List<Group> groups = new ArrayList<>();
        groups.add(testGroup);
        when(groupRepository.findAll()).thenReturn(groups);


        when(lookupTypeRepository.findByType(eq(LookupTypes.STATISTIC_TYPE))).thenReturn(lookupType);
        when(entityManager.createNativeQuery(any(String.class))).thenReturn(query);
        when(query.getSingleResult()).thenReturn(BigInteger.ONE);
        groupStatisticService.generateGroupStatistic(startDate, endDate, StatisticPeriod.MONTH);

        // There should be 2 results returned
        verify(query, Mockito.times(2)).setParameter(eq("startDate"), eq(startDate));
        verify(query, Mockito.times(2)).setParameter(eq("endDate"), eq(endDate));

        verify(query, Mockito.times(2)).setParameter(eq("groupId"), eq(testGroup.getId()));
        verify(query, Mockito.times(2)).getSingleResult();

        verify(groupStatisticRepository, Mockito.times(2)).save(any(GroupStatistic.class));

    }



    /**
     * Test: The retrieval of monthly statistics for groups with an invalid group
     * Fail: The exception is not thrown
     */
    @Test(expected = ResourceNotFoundException.class)
    public void testGetMonthlyGroupStatistics_UnknownGroup() throws ResourceNotFoundException {
        Group testGroup = TestUtils.createGroup(1L, "testGroup", creator);

        when(groupRepository.findOne(eq(testGroup.getId()))).thenReturn(testGroup);

        groupStatisticService.getMonthlyGroupStatistics(null);

        verify(groupStatisticRepository.findByGroupAndStatisticPeriod(eq(testGroup), eq(StatisticPeriod.MONTH)));

    }





}
