package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GpLetterRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 15/02/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class GpLetterRepositoryTest {

    @Inject
    private GpLetterRepository gpLetterRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindByClaimedPracticeCode() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setClaimedPracticeCode("ABC123");
        gpLetter.setGpPostcode("ABC 123");

        gpLetterRepository.save(gpLetter);

        List<GpLetter> gps = gpLetterRepository.findByClaimedPracticeCode(gpLetter.getClaimedPracticeCode());
        Assert.assertEquals("There should be 1 gp letter", 1, gps.size());
        Assert.assertTrue("The GP should be the one created", gps.get(0).equals(gpLetter));
    }

    @Test
    public void testFindByPostcode() {
        GpLetter gpLetter = new GpLetter();
        gpLetter.setGpPostcode("ABC 123");

        gpLetterRepository.save(gpLetter);

        List<GpLetter> gps = gpLetterRepository.findByPostcode(gpLetter.getGpPostcode());
        Assert.assertEquals("There should be 1 gp letter", 1, gps.size());
        Assert.assertTrue("The GP should be the one created", gps.get(0).equals(gpLetter));
    }

}
