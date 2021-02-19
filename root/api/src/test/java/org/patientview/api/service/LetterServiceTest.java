package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceReference;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.LetterServiceImpl;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
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

    @Mock
    UserService userService;

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

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);
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

    @Test
    public void testImportLetter_patientFromAnotherGroup_shouldFail() throws Exception {
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
        Group patientGroup = TestUtils.createGroup("PatientGroup");
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, patientGroup, patient);
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
        FhirLink fhirLink = TestUtils.createFhirLink(patient, identifier, patientGroup);

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

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);
        when(apiPatientService.get(eq(fhirLink.getResourceId()))).thenReturn(existingPatient);
        when(groupRepository.findByCode(eq(fhirDocumentReference.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirDocumentReference.getIdentifier()))).thenReturn(identifiers);
        when(Util.getFhirLink(eq(group), eq(identifier.getIdentifier()), eq(patient.getFhirLinks())))
                .thenReturn(patient.getFhirLinks().iterator().next());

        ServerResponse serverResponse = letterService.importLetter(fhirDocumentReference);

        Assert.assertTrue(
                "Should fail, got '" + serverResponse.getErrorMessage() + "'", !serverResponse.isSuccess());
        Assert.assertTrue("Should have correct error message, got '"
                        + serverResponse.getErrorMessage() + "'",
                serverResponse.getErrorMessage().contains("patient not a member of imported group"));

        verify(fhirResource, times(0)).createEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"));
        verify(fhirResource, times(0)).updateEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"), any(UUID.class));
        verify(documentReferenceService, times(0)).add(eq(fhirDocumentReference), eq(fhirLink));
        verify(userRepository, times(0)).save(eq(patient));
    }

    @Test
    public void testImportLetter_patientOutsideImporterGroup_shouldFail() throws Exception {
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

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(false);

        when(apiPatientService.get(eq(fhirLink.getResourceId()))).thenReturn(existingPatient);
        when(groupRepository.findByCode(eq(fhirDocumentReference.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirDocumentReference.getIdentifier()))).thenReturn(identifiers);
        when(Util.getFhirLink(eq(group), eq(identifier.getIdentifier()), eq(patient.getFhirLinks())))
                .thenReturn(patient.getFhirLinks().iterator().next());

        ServerResponse serverResponse = letterService.importLetter(fhirDocumentReference);

        Assert.assertTrue(
                "Should fail, got '" + serverResponse.getErrorMessage() + "'", !serverResponse.isSuccess());
        Assert.assertTrue("Should have correct error message, got '"
                + serverResponse.getErrorMessage() + "'", serverResponse.getErrorMessage().contains("Forbidden"));

        verify(fhirResource, times(0)).createEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"));
        verify(fhirResource, times(0)).updateEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"), any(UUID.class));
        verify(documentReferenceService, times(0)).add(eq(fhirDocumentReference), eq(fhirLink));
        verify(userRepository, times(0)).save(eq(patient));
    }
}
