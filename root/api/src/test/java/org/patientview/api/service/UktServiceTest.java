package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.UktServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
public class UktServiceTest {

    User creator;

    @Mock
    AuditRepository auditRepository;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    EncounterService encounterService;

    @Mock
    GroupService groupService;

    @InjectMocks
    UktService uktService = new UktServiceImpl();

    @Mock
    Properties properties;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testImport() throws ResourceNotFoundException, ResourceForbiddenException, URISyntaxException,
            FhirResourceException {

        User user = TestUtils.createUser("testUser");
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        String path = Thread.currentThread().getContextClassLoader().getResource("ukt").getPath();

        when(properties.getProperty("ukt.import.directory")).thenReturn(path);
        when(properties.getProperty("ukt.import.filename")).thenReturn("uktstatus.gpg.txt");
        when(identifierRepository.findByValue(identifier.getIdentifier())).thenReturn(identifiers);

        uktService.importData();
    }
}
