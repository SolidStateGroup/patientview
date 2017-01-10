package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Pathway;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.PathwayTypes;
import org.patientview.persistence.repository.PathwayRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 30/12/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class PathwayRepositoryTest {

    @Inject
    private PathwayRepository pathwayRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void givenPathwayRepo_whenSave_thenIdAssigned() {
        User user = dataTestUtils.createUser("testUser");

        Pathway pathway = new Pathway();
        pathway.setUser(user);
        pathway.setPathwayType(PathwayTypes.DONORPATHWAY);
        pathway.setCreator(user);
        pathway.setLastUpdater(user);
        Pathway create = pathwayRepository.save(pathway);

        Pathway found = pathwayRepository.findOne(create.getId());
        Assert.assertNotNull("Should have found pathway", found);
        Assert.assertNotNull("Should have created date set", found.getCreated());
        Assert.assertNotNull("Should have updated date set", found.getLastUpdate());
    }
}
