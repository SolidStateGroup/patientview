package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Category;
import org.patientview.persistence.repository.CategoryRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/06/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class CategoryRepositoryTest {

    @Inject
    CategoryRepository categoryRepository;

    @Test
    public void testFindVisible() {
        Category category1 = new Category(1, "icd10a", "friendlya");
        category1.setHidden(true);
        Category category2 = new Category(2, "icd10b", "friendlyb");

        categoryRepository.save(category1);
        categoryRepository.save(category2);

        List<Category> categories = categoryRepository.findVisible();

        Assert.assertEquals("There should be 1 returned Category", 1, categories.size());
        Assert.assertEquals("The Category should be the one created", category2.getNumber(),
                categories.get(0).getNumber());
    }
}
