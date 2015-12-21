package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.FoodDiary;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FoodDiaryRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 21/12/2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class FoodDiaryRepositoryTest {

    @Inject
    private FoodDiaryRepository foodDiaryRepository;

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

        FoodDiary foodDiary = new FoodDiary(user, "red meat", new Date());
        FoodDiary saved = foodDiaryRepository.save(foodDiary);

        List<FoodDiary> foodDiaries = foodDiaryRepository.findByUser(user);
        Assert.assertNotNull("There should be a saved food diary", saved);
        Assert.assertEquals("There should be 1 food diary", 1, foodDiaries.size());
        Assert.assertTrue("The food diary should be the one created", foodDiaries.get(0).equals(foodDiary));
    }
}
