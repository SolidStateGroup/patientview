package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.AlertObservationHeading;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.AlertObservationHeadingRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/12/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class AlertObservationHeadingRepositoryTest {

    @Inject
    private AlertObservationHeadingRepository alertObservationHeadingRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindByUser() {
        User user = dataTestUtils.createUser("testUser");
        ObservationHeading observationHeading = dataTestUtils.createObservationHeading("observationHeading");

        AlertObservationHeading alertObservationHeading = new AlertObservationHeading();
        alertObservationHeading.setUser(user);
        alertObservationHeading.setObservationHeading(observationHeading);
        alertObservationHeading.setWebAlert(true);
        alertObservationHeading.setWebAlertViewed(false);
        alertObservationHeading.setEmailAlert(true);
        alertObservationHeading.setEmailAlertSent(false);
        alertObservationHeadingRepository.save(alertObservationHeading);

        List<AlertObservationHeading> alertObservationHeadings = alertObservationHeadingRepository.findByUser(user);
        Assert.assertEquals("There should be 1 alert observation heading available", 1,
                alertObservationHeadings.size());
        Assert.assertTrue("The alert observation heading should be the one created",
                alertObservationHeadings.get(0).equals(alertObservationHeading));
    }
}
