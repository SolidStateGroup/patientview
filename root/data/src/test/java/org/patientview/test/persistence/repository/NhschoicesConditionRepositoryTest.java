package org.patientview.test.persistence.repository;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.NhschoicesCondition;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.NhschoicesConditionRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class NhschoicesConditionRepositoryTest {

    @Inject
    NhschoicesConditionRepository nhschoicesConditionRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindOneByCode() {
        List<NhschoicesCondition> conditions = new ArrayList<>();
        conditions.add(new NhschoicesCondition("code1", "name1", "uri1"));
        conditions.add(new NhschoicesCondition("code2", "name2", "uri2"));

        nhschoicesConditionRepository.saveAll(conditions);

        NhschoicesCondition foundCondition = nhschoicesConditionRepository.findOneByCode("code1");

        Assert.assertTrue("1 NhschoicesCondition should be returned", foundCondition != null);
        Assert.assertEquals("Correct NhschoicesCondition should be returned", foundCondition.getCode(), "code1");
    }

    @Test
    public void testDeleteByCode() {
        List<NhschoicesCondition> conditions = new ArrayList<>();
        conditions.add(new NhschoicesCondition("code1", "name1", "uri1"));
        conditions.add(new NhschoicesCondition("code2", "name2", "uri2"));
        conditions.add(new NhschoicesCondition("code3", "name3", "uri3"));

        nhschoicesConditionRepository.saveAll(conditions);

        List<NhschoicesCondition> foundConditions = nhschoicesConditionRepository.findAll();
        Assert.assertEquals("3 NhschoicesConditions should be returned", 3, foundConditions.size());

        List<String> codesToDelete = new ArrayList<>();
        codesToDelete.add("code1");
        codesToDelete.add("code2");

        nhschoicesConditionRepository.deleteByCode(codesToDelete);

        foundConditions = nhschoicesConditionRepository.findAll();
        Assert.assertEquals("1 NhschoicesConditions should be returned", 1, foundConditions.size());
    }
}
