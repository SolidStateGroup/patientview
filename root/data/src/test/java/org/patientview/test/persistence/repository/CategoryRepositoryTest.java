package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Category;
import org.patientview.persistence.repository.CategoryRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    @Test
    public void testFindAllFiltered() {
        Category category1 = new Category(1, "icd10a", "friendlya");
        category1.setHidden(true);
        Category category2 = new Category(2, "icd10b", "friendlyb");
        categoryRepository.save(category1);
        categoryRepository.save(category2);

        PageRequest pageable = PageRequest.of(0, 999);

        Page<Category> categories = categoryRepository.findAllFiltered("%%", pageable);
        Assert.assertEquals("There should be 2 categories found", 2, categories.getContent().size());
    }

    @Test
    public void testFindAllFilterText() {
        Category category1 = new Category(10, "icd10a", "friendlya");
        category1.setHidden(true);
        Category category2 = new Category(20, "icd10b", "friendlyb");
        category1 = categoryRepository.save(category1);
        category2 = categoryRepository.save(category2);

        PageRequest pageable = PageRequest.of(0, 999);

        Page<Category> categories = categoryRepository.findAllFiltered("%FRIENDLYA%", pageable);
        Assert.assertEquals("There should be 1 category returned", 1, categories.getContent().size());
        Assert.assertTrue("The category should be the one created", categories.getContent().get(0).equals(category1));

        categories = categoryRepository.findAllFiltered("%ICD10B%", pageable);
        Assert.assertEquals("There should be 1 category returned", 1, categories.getContent().size());
        Assert.assertTrue("The category should be the one created", categories.getContent().get(0).equals(category2));

        categories = categoryRepository.findAllFiltered("%2%", pageable);
        Assert.assertEquals("There should be 1 category returned", 1, categories.getContent().size());
        Assert.assertTrue("The category should be the one created", categories.getContent().get(0).equals(category2));
    }
}
