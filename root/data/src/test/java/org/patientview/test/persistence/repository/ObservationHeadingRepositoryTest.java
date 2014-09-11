package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Tests concerned with retrieving the correct news for a user.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class ObservationHeadingRepositoryTest {

    @Inject
    DataTestUtils dataTestUtils;

    @Inject
    ObservationHeadingRepository observationHeadingRepository;

    User creator;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindAll() {
        ObservationHeading observationHeading = dataTestUtils.createObservationHeading("OBS1");

        PageRequest pageable = new PageRequest(0, Integer.MAX_VALUE);
        Page<ObservationHeading> observationHeadings = observationHeadingRepository.findAll(pageable);

        Assert.assertEquals("There should be 1 observation heading", 1, observationHeadings.getContent().size());
        Assert.assertTrue("The observation heading should be the one created",
                observationHeadings.getContent().get(0).equals(observationHeading));
    }
}
