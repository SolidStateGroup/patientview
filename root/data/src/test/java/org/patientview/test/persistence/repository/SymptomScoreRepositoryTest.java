package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.SymptomScore;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ScoreSeverity;
import org.patientview.persistence.repository.SymptomScoreRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class SymptomScoreRepositoryTest {

    @Inject
    SymptomScoreRepository symptomScoreRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindByUser() {
        User user = dataTestUtils.createUser("TestUser");

        SymptomScore symptomScore = new SymptomScore(user, 1.1, ScoreSeverity.LOW, new Date());
        symptomScoreRepository.save(symptomScore);

        List<SymptomScore> symptomScores = symptomScoreRepository.findByUser(user);
        Assert.assertEquals("There should be 1 symptom score", 1, symptomScores.size());
        Assert.assertTrue("The symptom score should be the one created", symptomScores.get(0).equals(symptomScore));
    }
}
