package org.patientview.api.service;

import org.hl7.fhir.instance.model.Address;
import org.hl7.fhir.instance.model.Patient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.UktServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.UktException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.EncounterService;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

/**
 * Test UKT/NHSBT import/export feed.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/01/2015
 */
public class UktServiceTest {

    @Mock
    ApiPatientService apiPatientService;

    @Mock
    AuditRepository auditRepository;

    @Mock
    EncounterService encounterService;

    @Mock
    GroupService groupService;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    Properties properties;

    @InjectMocks
    UktService uktService = new UktServiceImpl();

    @Mock
    UserRepository userRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    @Ignore("Jenkins fails to find resources")
    public void testImport() throws ResourceNotFoundException, FhirResourceException, UktException {

        User user = TestUtils.createUser("testUser");
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        String path = getClass().getResource("/ukt").getPath();

        when(properties.getProperty("ukt.import.enabled")).thenReturn("true");
        when(properties.getProperty("ukt.import.directory")).thenReturn(path);
        when(properties.getProperty("ukt.import.filename")).thenReturn("uktstatus.gpg.txt");
        when(identifierRepository.findByValue(identifier.getIdentifier())).thenReturn(identifiers);

        uktService.importData();

        verify(identifierRepository, Mockito.times(1)).findByValue(eq(identifier.getIdentifier()));
    }

    @Test(expected = UktException.class)
    @Ignore("Jenkins fails to find resources")
    public void testImport_nofile() throws ResourceNotFoundException, FhirResourceException, UktException {

        User user = TestUtils.createUser("testUser");
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        String path = getClass().getResource("/ukt").getPath();

        when(properties.getProperty("ukt.import.enabled")).thenReturn("true");
        when(properties.getProperty("ukt.import.directory")).thenReturn(path);
        when(properties.getProperty("ukt.import.filename")).thenReturn("WRONGFILE.txt");
        when(identifierRepository.findByValue(identifier.getIdentifier())).thenReturn(identifiers);

        uktService.importData();

        verify(identifierRepository, Mockito.times(1)).findByValue(eq(identifier.getIdentifier()));
    }

    @Test
    @Ignore("Jenkins fails to find resources")
    public void testExport() throws ResourceNotFoundException, FhirResourceException, UktException {

        String path = getClass().getResource("/ukt").getPath();
        UUID resourceId = UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287");
        
        // example patient user
        User user = TestUtils.createUser("testUser");
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.IDENTIFIER);
        Lookup lookup = TestUtils.createLookup(lookupType, IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        Set<Identifier> identifiers = new HashSet<>();
        identifiers.add(identifier);
        user.setIdentifiers(identifiers);
        
        // handle stripping brackets
        user.setForename("fore]name");        
        // handle stripping quotes and double spaces
        user.setSurname("sur\" name");      
        // example date of birth
        user.setDateOfBirth(new Date());
        
        // fhirLink for postcode from FHIR
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(user);
        fhirLink.setResourceId(resourceId);
        user.setFhirLinks(new HashSet<FhirLink>());
        user.getFhirLinks().add(fhirLink);
        
        List<User> users = new ArrayList<>();
        users.add(user);

        // FHIR patient object required to retrieve postcode
        Patient patient = new Patient();
        Address address = patient.addAddress();
        address.setZipSimple("AB123CDE");        

        when(properties.getProperty("ukt.export.enabled")).thenReturn("true");
        when(properties.getProperty("ukt.export.directory")).thenReturn(path);
        when(properties.getProperty("ukt.export.filename")).thenReturn("ukt_rpv_export.txt");

        //Get the initial page
        PageRequest pageRequest = new PageRequest(0, 1000);

        when(userRepository.findAllPatients(pageRequest)).thenReturn(new PageImpl<>(users));
        when(apiPatientService.get(eq(resourceId))).thenReturn(patient);

        uktService.exportData();

        verify(userRepository, Mockito.times(2)).findAllPatients(pageRequest);
    }
}
