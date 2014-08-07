package org.patientview.api.service;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.api.config.TestPersistenceConfig;
import org.patientview.api.service.impl.GroupServiceImpl;
import org.patientview.api.service.impl.GroupStatisticsServiceImpl;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.test.util.DataTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

/**
 * So this is kind of an integration test but to can't win them all.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
// ApplicationContext will be loaded from the static inner ContextConfiguration class
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
@EnableAutoConfiguration
public class GroupStatisticsServiceTest {

    User creator;

    @Inject
    GroupStatisticService groupStatisticService;

    @Inject
    LookupRepository lookupRepository;

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

    @Test
    public void testGenerateGroupStatistic() {

        Lookup lookup = dataTestUtils.createLookup("PATIENT", LookupTypes.STATISTICS_TYPE, creator);
        lookup.setDescription("SELECT COUNT(1) FROM pv_user_group_role WHERE creation_date BETWEEN :startDate AND :endDate AND group_id = :groupId");
        lookupRepository.save(lookup);

        lookup = dataTestUtils.createLookup("LOGON", LookupTypes.STATISTICS_TYPE, creator);
        lookup.setDescription("SELECT COUNT(1) FROM pv_audit WHERE creation_date BETWEEN :startDate AND :endDate AND id > :groupId");
        lookupRepository.save(lookup);

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.roll(Calendar.MONTH, -1);
        Date startDate =  calendar.getTime();;
        groupStatisticService.generateGroupStatistic(startDate, endDate, StatisticPeriod.MONTH);


    }

}