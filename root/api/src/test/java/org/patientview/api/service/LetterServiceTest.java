package org.patientview.api.service;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.DocumentReference;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.service.impl.LetterServiceImpl;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LetterTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.DocumentReferenceService;
import org.patientview.test.util.TestUtils;
import org.patientview.util.Util;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class LetterServiceTest {

    User creator;

    @Mock
    ApiPatientService apiPatientService;

    @Mock
    DocumentReferenceService documentReferenceService;

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupService groupService;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    FhirResource fhirResource;

    @InjectMocks
    LetterService letterService = new LetterServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
        PowerMockito.mockStatic(Util.class);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetByUserId() {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        user.setIdentifiers(new HashSet<Identifier>());

        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        fhirLink.setResourceType(ResourceType.Patient.name());
        fhirLink.setGroup(group);

        DocumentReference documentReference = new DocumentReference();

        DateTime letterDate = new DateTime();
        DateAndTime date = new DateAndTime(new Date());
        letterDate.setValue(date);
        documentReference.setCreated(letterDate);

        CodeableConcept type = new CodeableConcept();
        type.setTextSimple(LetterTypes.CLINIC_LETTER.toString());
        documentReference.setType(type);

        documentReference.setDescriptionSimple("LETTER_CONTENT");

        List<DocumentReference> documentReferences = new ArrayList<>();
        documentReferences.add(documentReference);

        try {
            when(userRepository.findOne(Matchers.eq(user.getId()))).thenReturn(user);
            when(fhirResource.findResourceByQuery(any(String.class), eq(DocumentReference.class)))
                    .thenReturn(documentReferences);

            List<FhirDocumentReference> fhirDocumentReferences = letterService.getByUserId(user.getId());
            Assert.assertEquals("Should return 1 FhirDocumentReference", 1, fhirDocumentReferences.size());
            Assert.assertEquals("Should have correct content", "LETTER_CONTENT",
                    fhirDocumentReferences.get(0).getContent());

        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException: " + rnf.getMessage());
        } catch (FhirResourceException fre) {
            Assert.fail("FhirResourceException: " + fre.getMessage());
        }
    }

    @Test
    public void testImportLetter() throws Exception {
        // auth
        Group group = TestUtils.createGroup("testGroup");
        Role staffRole = TestUtils.createRole(RoleName.IMPORTER);
        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // existing fhirlink
        FhirLink fhirLink = TestUtils.createFhirLink(patient, identifier, group);

        // existing fhir Patient associated with existing fhirLink
        Patient existingPatient = new Patient();
        ResourceReference careProvider = existingPatient.addCareProvider();
        careProvider.setReferenceSimple("uuid");
        careProvider.setDisplaySimple(UUID.randomUUID().toString());

        // letter to import
        org.patientview.persistence.model.FhirDocumentReference fhirDocumentReference
                = new org.patientview.persistence.model.FhirDocumentReference();
        fhirDocumentReference.setGroupCode(group.getCode());
        fhirDocumentReference.setIdentifier(identifier.getIdentifier());
        fhirDocumentReference.setContent("some content");
        fhirDocumentReference.setType("some type");
        fhirDocumentReference.setDate(new Date());

        when(apiPatientService.get(eq(fhirLink.getResourceId()))).thenReturn(existingPatient);
        when(groupRepository.findByCode(eq(fhirDocumentReference.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirDocumentReference.getIdentifier()))).thenReturn(identifiers);
        when(Util.getFhirLink(eq(group), eq(identifier.getIdentifier()), eq(patient.getFhirLinks())))
                .thenReturn(patient.getFhirLinks().iterator().next());

        ServerResponse serverResponse = letterService.importLetter(fhirDocumentReference);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertEquals("Should have done message", "done", serverResponse.getSuccessMessage());

        verify(fhirResource, times(0)).createEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"));
        verify(fhirResource, times(0)).updateEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"), any(UUID.class));
        verify(documentReferenceService, times(1)).add(eq(fhirDocumentReference), eq(fhirLink));
        verify(userRepository, times(0)).save(eq(patient));
    }
}
