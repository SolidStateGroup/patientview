package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.sql.DataSource;
import org.patientview.api.config.TestPersistenceConfig;
import org.patientview.api.model.GroupStatisticTO;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.impl.GroupStatisticsServiceImpl;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.patientview.persistence.model.enums.StatisticType;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.test.util.DataTestUtils;
import org.patientview.test.util.TestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
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
    DataSource dataSource;

    @Inject
    DataTestUtils dataTestUtils;

    @Inject
    FhirResource fhirResource;

    @Inject
    GroupStatisticService groupStatisticService;

    @Inject
    LookupRepository lookupRepository;

    @Inject
    LookupTypeRepository lookupTypeRepository;

    @Configuration
    @Import(TestPersistenceConfig.class)
    public static class config {
        @Bean(name = "groupStatisticService")
        public GroupStatisticService groupStatisticServiceBean() {
            return new GroupStatisticsServiceImpl();
        }
        @Bean(name = "fhirResource")
        public FhirResource fhirResourceBean() {
            return new FhirResource();
        }
        @Bean(name = "fhir")
        public DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }
    }

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("user");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: Create 2 lookups that are flagged as statistics. This will then process the statistics against a group.
     * Fail: The statistics are not created.
     * @throws ResourceNotFoundException
     */
    @Test
    public void testGenerateGroupStatistic() throws ResourceNotFoundException {

        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);
        Group testGroup = dataTestUtils.createGroup("TestStatisticGroup");

        createStatisticLookups();

        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.roll(Calendar.MONTH, -1);
        Date startDate =  calendar.getTime();
        groupStatisticService.generateGroupStatistic(startDate, endDate, StatisticPeriod.MONTH);

        try {
            List<GroupStatisticTO> groupStatistics = groupStatisticService.getMonthlyGroupStatistics(testGroup.getId());
            // Expect the generate statistics to have create 2 statistics from the lookups below
            Assert.assertEquals("Should have one date of statistics", 1, groupStatistics.size());
        } catch (ResourceForbiddenException rfe) {
            Assert.fail("ResourceForbiddenException: " + rfe.getMessage());
        }
    }

    private void createStatisticLookups() {
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.STATISTIC_TYPE);
        lookupType.setCreator(creator);
        lookupType = lookupTypeRepository.save(lookupType);

        Lookup lookup = new Lookup();
        lookup.setValue(StatisticType.PATIENT_COUNT.name());
        lookup.setDescription("SELECT COUNT(1) FROM pv_user_group_role WHERE creation_date BETWEEN :startDate AND :endDate AND group_id = :groupId");
        lookup.setLookupType(lookupType);
        lookup.setId(1L);
        lookup.setCreator(creator);
        lookupRepository.save(lookup);

        lookup = new Lookup();
        lookup.setValue(StatisticType.LOGGED_ON_COUNT.name());
        lookup.setDescription("SELECT COUNT(1) FROM pv_audit WHERE creation_date BETWEEN :startDate AND :endDate AND id > :groupId");
        lookup.setLookupType(lookupType);
        lookup.setId(2L);
        lookup.setCreator(creator);
        lookupRepository.save(lookup);
    }
}
