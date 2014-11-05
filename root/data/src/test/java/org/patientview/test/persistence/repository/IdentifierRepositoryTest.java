package org.patientview.test.persistence.repository;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.persistence.config.TestPersistenceConfig;
import org.patientview.test.util.DataTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestPersistenceConfig.class})
@Transactional
public class IdentifierRepositoryTest {

    @Inject
    IdentifierRepository identifierRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    DataTestUtils dataTestUtils;

    User creator;

    @Before
    public void setup() {
        creator = dataTestUtils.createUser("testCreator");
    }

    @Test
    public void testFindPatientByNhsNo() {

        String testNhsNumber = "324234234";

        User user = dataTestUtils.createUser("testUser");
        Lookup lookup = dataTestUtils.createLookup("NHS_NUMBER", LookupTypes.IDENTIFIER);

        user.setIdentifiers(new HashSet<Identifier>());

        Identifier identifier = new Identifier();
        identifier.setIdentifier(testNhsNumber);
        identifier.setIdentifierType(lookup);
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);

        userRepository.save(user);

        identifier = identifierRepository.findByTypeAndValue(testNhsNumber, lookup);

        Assert.assertTrue("There is 1 identifier returned", identifier != null);
    }

    @Test
    public void testFindByValue() {

        String testNhsNumber = "324234234";

        User user = dataTestUtils.createUser("testUser");
        Lookup lookup = dataTestUtils.createLookup("NHS_NUMBER", LookupTypes.IDENTIFIER);

        user.setIdentifiers(new HashSet<Identifier>());

        Identifier identifier = new Identifier();
        identifier.setIdentifier(testNhsNumber);
        identifier.setIdentifierType(lookup);
        identifier.setUser(user);
        user.getIdentifiers().add(identifier);

        userRepository.save(user);

        List<Identifier> identifiers = identifierRepository.findByValue(testNhsNumber);

        Assert.assertEquals("There is 1 identifier returned", 1, identifiers.size());
    }
}