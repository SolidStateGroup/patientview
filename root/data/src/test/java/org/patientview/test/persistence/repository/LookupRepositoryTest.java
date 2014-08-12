package org.patientview.test.persistence.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Tests for lookup types (parent of lookup)
 * Created by jamesr@solidstategroup.com
 * Created on 01/07/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class LookupRepositoryTest {

    @Inject
    LookupTypeRepository lookupTypeRepository;
    @Inject
    LookupRepository lookupRepository;
    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup () {
        creator = dataTestUtils.createUser("testCreator");
    }

    /**
     * Test: Create a lookup type and value and attempt to retrieve by type and then by type and value
     * Fail: Either lookup type or value creation is unsuccessful
     */
    @Ignore
    @Test
    public void testCreateLookupTypeAndValue() {

        // Create a lookup type item
        dataTestUtils.createLookup("STAFF", LookupTypes.FEATURE_TYPE, creator);
        LookupType getLookupType = lookupTypeRepository.findByType(LookupTypes.GROUP);

        Assert.assertTrue("LookupType should be created", getLookupType.getType().equals("FEATURE_TYPE"));
        Assert.assertNotNull("Lookup should be created", lookupRepository.findByTypeAndValue(LookupTypes.FEATURE_TYPE, "STAFF"));
    }
}
