package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
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
import java.util.Arrays;
import java.util.List;

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

        PageRequest pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Page<ObservationHeading> observationHeadings = observationHeadingRepository.findAll(pageable);

        Assert.assertEquals("There should be 1 observation heading", 1, observationHeadings.getContent().size());
        Assert.assertTrue("The observation heading should be the one created",
                observationHeadings.getContent().get(0).equals(observationHeading));
    }

    @Test
    public void testFindByCode() {
        ObservationHeading observationHeading = dataTestUtils.createObservationHeading("OBS1");

        List<ObservationHeading> observationHeadings = observationHeadingRepository.findByCode("OBS1");
        Assert.assertEquals("There should be 1 observation heading available", 1, observationHeadings.size());
        Assert.assertTrue("The observation heading should be the one created",
                observationHeadings.get(0).equals(observationHeading));
    }

    @Test
    public void testFindAllByCode() {
        ObservationHeading observationHeading = dataTestUtils.createObservationHeading("OBS1");
        ObservationHeading observationHeading2 = dataTestUtils.createObservationHeading("OBS2");

        List<ObservationHeading> observationHeadings = observationHeadingRepository.findAllByCode(
                Arrays.asList(observationHeading.getCode().toLowerCase(), observationHeading2.getCode().toLowerCase()));
        Assert.assertEquals("There should be 2 observation heading available", 2, observationHeadings.size());
        Assert.assertTrue("The observation heading should be the one created",
                observationHeadings.get(0).equals(observationHeading));
    }

    @Test
    public void testSave() {
        ObservationHeading observationHeading = dataTestUtils.createObservationHeading("OBS1");

        Group group = dataTestUtils.createGroup("SPECIALTY_GROUP");
        ObservationHeadingGroup observationHeadingGroup = new ObservationHeadingGroup();
        observationHeadingGroup.setObservationHeading(observationHeading);
        observationHeadingGroup.setGroup(group);
        observationHeadingGroup.setPanel(1L);
        observationHeadingGroup.setPanelOrder(2L);

        observationHeading.getObservationHeadingGroups().add(observationHeadingGroup);
        observationHeadingRepository.save(observationHeading);

        List<ObservationHeading> observationHeadings = observationHeadingRepository.findByCode("OBS1");
        Assert.assertEquals("There should be 1 observation heading available", 1, observationHeadings.size());
        Assert.assertTrue("The observation heading should be the one created",
                observationHeadings.get(0).equals(observationHeading));

        ObservationHeading entityObservationHeading = observationHeadings.get(0);
        Assert.assertEquals("There should be one linked group (ObservationHeadingGroup)", 1,
                entityObservationHeading.getObservationHeadingGroups().size());
        Assert.assertEquals("The linked group should be the one created", group,
                entityObservationHeading.getObservationHeadingGroups().iterator().next().getGroup());
    }
}
