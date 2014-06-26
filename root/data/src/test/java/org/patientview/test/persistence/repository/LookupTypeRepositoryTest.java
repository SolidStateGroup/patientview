package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Date;

/**
 * Tests for lookup types (parent of lookup)
 * Created by jamesr@solidstategroup.com
 * Created on 26/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class LookupTypeRepositoryTest {

    @Inject
    LookupTypeRepository lookupTypeRepository;
    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
    }

    /**
     * Test: Create a lookup type and attempt to retrieve by type
     * Fail: The lookup type cannot be created or retrieved by type
     */
    @Test
    public void testCreateAndRetrieveLookupType() {

        // Create a lookup type item
        LookupType lookupType = new LookupType();
        lookupType.setType("NEWTYPE");
        lookupType.setDescription("a new kind of lookup type");
        lookupType.setCreator(creator);
        lookupType.setCreated(new Date());
        lookupTypeRepository.save(lookupType);

        LookupType getLookupType = lookupTypeRepository.getByType("NEWTYPE");
        Assert.assertTrue("LookupType should be created and retrieved", getLookupType.getType().equals("NEWTYPE"));
    }
}
