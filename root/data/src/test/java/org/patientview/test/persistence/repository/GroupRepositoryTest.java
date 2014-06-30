package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.patientview.test.util.TestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class GroupRepositoryTest {
    @Inject
    GroupRepository groupRepository;
    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
    }

    // todo: fix this test as part of group administration
    @Test
    @Ignore("M2M self relationship not persisting correctly, needs checking")
    public void testCreateGroup() {

        // Create a group
        Group childGroup = TestUtils.createGroup(1L, "CHILD_GROUP", creator);
        Group parentGroup = TestUtils.createGroup(2L, "PARENT_GROUP", creator);
        groupRepository.save(childGroup);
        groupRepository.save(parentGroup);

        childGroup.getParentGroups().add(parentGroup);
        //parentGroup.getChildGroups().add(childGroup);
        groupRepository.save(childGroup);
        //groupRepository.save(parentGroup);

        Assert.assertTrue("Should have parent group", childGroup.getParentGroups().size() == 1);
        Assert.assertTrue("Should have child group", parentGroup.getChildGroups().size() == 1);
    }
}
