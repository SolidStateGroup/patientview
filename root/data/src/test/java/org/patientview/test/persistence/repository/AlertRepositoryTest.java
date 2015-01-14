package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.repository.AlertRepository;
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
public class AlertRepositoryTest {

    @Inject
    private AlertRepository alertRepository;

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

        Alert alert = new Alert();
        alert.setUser(user);
        alert.setObservationHeading(observationHeading);
        alert.setWebAlert(true);
        alert.setWebAlertViewed(false);
        alert.setEmailAlert(true);
        alert.setEmailAlertSent(false);
        alert.setAlertType(AlertTypes.RESULT);
        alertRepository.save(alert);

        List<Alert> alerts = alertRepository.findByUser(user);
        Assert.assertEquals("There should be 1 alert", 1, alerts.size());
        Assert.assertTrue("The alert should be the one created", alerts.get(0).equals(alert));
    }
}
