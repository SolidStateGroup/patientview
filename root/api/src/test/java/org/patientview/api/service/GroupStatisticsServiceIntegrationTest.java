package org.patientview.api.service;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.api.config.TestPersistenceConfig;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.GroupStatisticsServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.patientview.persistence.model.enums.StatisticType;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.test.util.DataTestUtils;
import org.patientview.test.util.TestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Integration test for the statistic count. We needed this for the interaction with the entity manager. However
 * ignore this when things slow down.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
// ApplicationContext will be loaded from the static inner ContextConfiguration class
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
public class GroupStatisticsServiceIntegrationTest {

    User creator;

    @Inject
    GroupStatisticService groupStatisticService;

    @Inject
    LookupRepository lookupRepository;

    @Inject
    LookupTypeRepository lookupTypeRepository;

    @Inject
    DataTestUtils dataTestUtils;

    @Configuration
    @Import(TestPersistenceConfig.class)
    static class config {
        @Bean(name = "groupStatisticService")
        public GroupStatisticService groupStatisticServiceBean() {
            return new GroupStatisticsServiceImpl();
        }
    }

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("user");
    }

    /**
     * Test: Create 2 lookups that are flagged as statistics. This will then process the statistics against a group.
     * Fail: The statistics are not created.
     * @throws ResourceNotFoundException
     */
    @Test
    public void testGenerateGroupStatistic() throws ResourceNotFoundException {

        Group testGroup = dataTestUtils.createGroup("TestStatisticGroup", creator);

        createStatisticLookups();

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.roll(Calendar.MONTH, -1);
        Date startDate =  calendar.getTime();;
        groupStatisticService.generateGroupStatistic(startDate, endDate, StatisticPeriod.MONTH);

        List<GroupStatistic> groupStatistics = groupStatisticService.getMonthlyGroupStatistics(testGroup.getId());

        // Expect the generate statistics to have create 2 statistics from the lookups below
        Assert.assertTrue("We have created 2 statistics for our group", groupStatistics.size() == 2);

    }

    private void createStatisticLookups() {
        LookupType lookupType = TestUtils.createLookupType(null, LookupTypes.STATISTIC_TYPE, creator);
        lookupTypeRepository.save(lookupType);

        Lookup lookup = new Lookup();
        lookup.setValue(StatisticType.PATIENT_COUNT.name());
        lookup.setDescription("SELECT COUNT(1) FROM pv_user_group_role WHERE creation_date BETWEEN :startDate AND :endDate AND group_id = :groupId");
        lookup.setLookupType(lookupType);
        lookupRepository.save(lookup);

        lookup = new Lookup();
        lookup.setValue(StatisticType.LOGON_COUNT.name());
        lookup.setDescription("SELECT COUNT(1) FROM pv_audit WHERE creation_date BETWEEN :startDate AND :endDate AND id > :groupId");
        lookup.setLookupType(lookupType);
        lookupRepository.save(lookup);

    }

}