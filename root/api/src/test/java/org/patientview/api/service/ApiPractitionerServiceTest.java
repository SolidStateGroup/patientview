package org.patientview.api.service;

import org.hl7.fhir.instance.model.HumanName;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
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
import org.patientview.api.builder.PatientBuilder;
import org.patientview.api.service.impl.ApiPractitionerServiceImpl;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.GpLetterService;
import org.patientview.service.PatientService;
import org.patientview.test.util.TestUtils;
import org.patientview.util.Util;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for ApiPractitionerService, used for importing practitioner in FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 07/03/2016
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class ApiPractitionerServiceTest {

    @Mock
    ApiPatientService apiPatientService;

    @InjectMocks
    ApiPractitionerService apiPractitionerService = new ApiPractitionerServiceImpl();

    @Mock
    private FhirLinkRepository fhirLinkRepository;

    @Mock
    private FhirResource fhirResource;

    @Mock
    private GpLetterService gpLetterService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private IdentifierRepository identifierRepository;

    @Mock
    private PatientBuilder patientBuilder;

    @Mock
    private PatientService patientService;

    @Mock
    private UserRepository userRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(Util.class);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testImportPractitioner_newPractitionerNewFhirLink() throws Exception {
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

        // created fhir patient database entry
        FhirDatabaseEntity patientFhirDatabaseEntity = new FhirDatabaseEntity();
        patientFhirDatabaseEntity.setLogicalId(UUID.randomUUID());

        // created fhir practitioner database entry
        FhirDatabaseEntity practitionerFhirDatabaseEntity = new FhirDatabaseEntity();
        practitionerFhirDatabaseEntity.setLogicalId(UUID.randomUUID());

        FhirPractitioner fhirPractitioner = new FhirPractitioner();
        fhirPractitioner.setName("practitioner");
        fhirPractitioner.setPostcode("AB1 23C");
        fhirPractitioner.setGroupCode("RENALB");
        fhirPractitioner.setIdentifier(identifier.getIdentifier());

        when(fhirResource.createEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"))).thenReturn(patientFhirDatabaseEntity);
        when(fhirResource.createEntity(any(Practitioner.class), eq(ResourceType.Practitioner.name()),
                eq("practitioner"))).thenReturn(practitionerFhirDatabaseEntity);
        when(groupRepository.findByCode(eq(fhirPractitioner.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirPractitioner.getIdentifier()))).thenReturn(identifiers);

        ServerResponse serverResponse = apiPractitionerService.importPractitioner(fhirPractitioner);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertEquals("Should have done message", "done", serverResponse.getSuccessMessage());

        verify(fhirResource, times(1)).createEntity(any(Practitioner.class),
                eq(ResourceType.Practitioner.name()), eq("practitioner"));
        verify(fhirResource, times(1)).createEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"));
        verify(fhirResource, times(1)).updateEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"), any(UUID.class));
        verify(gpLetterService, times(1)).createGpLetter(any(FhirLink.class), any(GpLetter.class));
        verify(userRepository, times(1)).save(eq(patient));
    }

    @Test
    public void testImportPractitioner_newPractitionerExistingFhirLink() throws Exception {
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

        // fhir Patient associated with existing fhirLink
        Patient existingPatient = new Patient();

        // created fhir patient database entry
        FhirDatabaseEntity patientFhirDatabaseEntity = new FhirDatabaseEntity();
        patientFhirDatabaseEntity.setLogicalId(UUID.randomUUID());

        // created fhir practitioner database entry
        FhirDatabaseEntity practitionerFhirDatabaseEntity = new FhirDatabaseEntity();
        practitionerFhirDatabaseEntity.setLogicalId(UUID.randomUUID());

        FhirPractitioner fhirPractitioner = new FhirPractitioner();
        fhirPractitioner.setName("practitioner");
        fhirPractitioner.setPostcode("AB1 23C");
        fhirPractitioner.setGroupCode("RENALB");
        fhirPractitioner.setIdentifier(identifier.getIdentifier());

        when(apiPatientService.get(eq(fhirLink.getResourceId()))).thenReturn(existingPatient);
        when(fhirResource.createEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"))).thenReturn(patientFhirDatabaseEntity);
        when(fhirResource.createEntity(any(Practitioner.class), eq(ResourceType.Practitioner.name()),
                eq("practitioner"))).thenReturn(practitionerFhirDatabaseEntity);
        when(groupRepository.findByCode(eq(fhirPractitioner.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirPractitioner.getIdentifier()))).thenReturn(identifiers);
        when(Util.getFhirLink(eq(group), eq(identifier.getIdentifier()), eq(patient.getFhirLinks())))
                .thenReturn(patient.getFhirLinks().iterator().next());

        ServerResponse serverResponse = apiPractitionerService.importPractitioner(fhirPractitioner);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertEquals("Should have done message", "done", serverResponse.getSuccessMessage());

        verify(fhirResource, times(1)).createEntity(any(Practitioner.class),
                eq(ResourceType.Practitioner.name()), eq("practitioner"));
        verify(fhirResource, times(0)).createEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"));
        verify(fhirResource, times(1)).updateEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"), any(UUID.class));
        verify(gpLetterService, times(1)).createGpLetter(any(FhirLink.class), any(GpLetter.class));
        verify(userRepository, times(0)).save(eq(patient));
    }

    @Test
    public void testImportPractitioner_updatePractitioner() throws Exception {
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

        // existing fhir Practitioner to update
        Practitioner existingPractitioner = new Practitioner();
        HumanName humanName = new HumanName();
        humanName.addFamilySimple("existingPractitioner");
        existingPractitioner.setName(humanName);

        // created fhir patient database entry
        FhirDatabaseEntity patientFhirDatabaseEntity = new FhirDatabaseEntity();
        patientFhirDatabaseEntity.setLogicalId(UUID.randomUUID());

        // created fhir practitioner database entry
        FhirDatabaseEntity practitionerFhirDatabaseEntity = new FhirDatabaseEntity();
        practitionerFhirDatabaseEntity.setLogicalId(UUID.randomUUID());

        FhirPractitioner fhirPractitioner = new FhirPractitioner();
        fhirPractitioner.setName("practitioner");
        fhirPractitioner.setPostcode("AB1 23C");
        fhirPractitioner.setGroupCode("RENALB");
        fhirPractitioner.setIdentifier(identifier.getIdentifier());

        when(apiPatientService.get(eq(fhirLink.getResourceId()))).thenReturn(existingPatient);
        when(fhirResource.createEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"))).thenReturn(patientFhirDatabaseEntity);
        when(fhirResource.createEntity(any(Practitioner.class), eq(ResourceType.Practitioner.name()),
                eq("practitioner"))).thenReturn(practitionerFhirDatabaseEntity);
        when(fhirResource.get(eq(UUID.fromString(careProvider.getDisplaySimple())), eq(ResourceType.Practitioner)))
                .thenReturn(existingPractitioner);
        when(groupRepository.findByCode(eq(fhirPractitioner.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirPractitioner.getIdentifier()))).thenReturn(identifiers);
        when(Util.getFhirLink(eq(group), eq(identifier.getIdentifier()), eq(patient.getFhirLinks())))
                .thenReturn(patient.getFhirLinks().iterator().next());

        ServerResponse serverResponse = apiPractitionerService.importPractitioner(fhirPractitioner);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertEquals("Should have done message", "done", serverResponse.getSuccessMessage());

        verify(fhirResource, times(0)).createEntity(any(Practitioner.class),
                eq(ResourceType.Practitioner.name()), eq("practitioner"));
        verify(fhirResource, times(0)).createEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"));
        verify(fhirResource, times(1)).get(eq(UUID.fromString(careProvider.getDisplaySimple())),
                eq(ResourceType.Practitioner));
        verify(fhirResource, times(0)).updateEntity(any(Patient.class),
                eq(ResourceType.Patient.name()), eq("patient"), any(UUID.class));
        verify(fhirResource, times(1)).updateEntity(any(Patient.class),
                eq(ResourceType.Practitioner.name()), eq("practitioner"), any(UUID.class));
        verify(gpLetterService, times(1)).createGpLetter(any(FhirLink.class), any(GpLetter.class));
        verify(userRepository, times(0)).save(eq(patient));
    }
}
