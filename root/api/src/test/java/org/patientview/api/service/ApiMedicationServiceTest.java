package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.ApiMedicationServiceImpl;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirMedicationStatement;
import org.patientview.persistence.model.FhirMedicationStatementRange;
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
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.FhirLinkService;
import org.patientview.service.MedicationService;
import org.patientview.service.PatientService;
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
 * Created on 11/09/2014
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class ApiMedicationServiceTest {

    User creator;

    @InjectMocks
    ApiMedicationService apiMedicationService = new ApiMedicationServiceImpl();

    @Mock
    FhirLinkService fhirLinkService;

    @Mock
    FhirResource fhirResource;

    @Mock
    GroupRepository groupRepository;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    MedicationService medicationService;

    @Mock
    PatientService patientService;

    @Mock
    UserService userService;

    private Date now;
    private Date weekAgo;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
        PowerMockito.mockStatic(Util.class);
        this.now = new Date();
        this.weekAgo = new org.joda.time.DateTime(now).minusWeeks(1).toDate();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testImportMedication() throws Exception {
        // auth
        Group group = TestUtils.createGroup("testGroup");
        group.setGroupType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.GROUP), "UNIT"));
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

        TestUtils.createFhirLink(patient, identifier, group);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirMedicationStatementRange
        FhirMedicationStatementRange fhirMedicationStatementRange = new FhirMedicationStatementRange();
        fhirMedicationStatementRange.setGroupCode("DSF01");
        fhirMedicationStatementRange.setIdentifier("1111111111");
        fhirMedicationStatementRange.setStartDate(weekAgo);
        fhirMedicationStatementRange.setEndDate(now);
        fhirMedicationStatementRange.setMedications(new ArrayList<FhirMedicationStatement>());

        // FhirMedication to insert
        FhirMedicationStatement fhirMedicationStatement = new FhirMedicationStatement();
        fhirMedicationStatement.setName("medicationName");
        fhirMedicationStatement.setDose("medicationDose");
        fhirMedicationStatementRange.getMedications().add(fhirMedicationStatement);

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(groupRepository.findByCode(eq(fhirMedicationStatementRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirMedicationStatementRange.getIdentifier())))
                .thenReturn(identifiers);
        when(medicationService.deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirMedicationStatementRange.getStartDate()), eq(fhirMedicationStatementRange.getEndDate())))
                .thenReturn(1);

        ServerResponse serverResponse = apiMedicationService.importMedication(fhirMedicationStatementRange);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct added success message, got '"
                + serverResponse.getSuccessMessage() + "'", serverResponse.getSuccessMessage().contains("added 1"));
        Assert.assertTrue("Should have correct deleted success message, got '"
                + serverResponse.getSuccessMessage() + "'", serverResponse.getSuccessMessage().contains("deleted 1"));

        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(medicationService, times(1)).add(eq(fhirMedicationStatement),
                eq(patient.getFhirLinks().iterator().next()));
        verify(medicationService, times(1)).deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirMedicationStatementRange.getStartDate()), eq(fhirMedicationStatementRange.getEndDate()));
    }

    @Test
    public void testImportMedication_addOnly() throws Exception {
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

        TestUtils.createFhirLink(patient, identifier, group);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirMedicationStatementRange
        FhirMedicationStatementRange fhirMedicationStatementRange = new FhirMedicationStatementRange();
        fhirMedicationStatementRange.setGroupCode("DSF01");
        fhirMedicationStatementRange.setIdentifier("1111111111");
        fhirMedicationStatementRange.setMedications(new ArrayList<FhirMedicationStatement>());

        // FhirMedication to insert
        FhirMedicationStatement fhirMedicationStatement = new FhirMedicationStatement();
        fhirMedicationStatement.setName("medicationName");
        fhirMedicationStatement.setDose("medicationDose");
        fhirMedicationStatementRange.getMedications().add(fhirMedicationStatement);

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(groupRepository.findByCode(eq(fhirMedicationStatementRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirMedicationStatementRange.getIdentifier())))
                .thenReturn(identifiers);

        ServerResponse serverResponse = apiMedicationService.importMedication(fhirMedicationStatementRange);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct added success message, got '"
                + serverResponse.getSuccessMessage() + "'", serverResponse.getSuccessMessage().contains("added 1"));
        Assert.assertTrue("Should have correct deleted success message, got '"
                + serverResponse.getSuccessMessage() + "'", !serverResponse.getSuccessMessage().contains("deleted 1"));

        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(medicationService, times(1)).add(eq(fhirMedicationStatement),
                eq(patient.getFhirLinks().iterator().next()));
        verify(medicationService, times(0)).deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirMedicationStatementRange.getStartDate()), eq(fhirMedicationStatementRange.getEndDate()));
    }

    @Test
    public void testImportMedication_deleteOnly() throws Exception {
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

        TestUtils.createFhirLink(patient, identifier, group);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirMedicationStatementRange
        FhirMedicationStatementRange fhirMedicationStatementRange = new FhirMedicationStatementRange();
        fhirMedicationStatementRange.setGroupCode("DSF01");
        fhirMedicationStatementRange.setIdentifier("1111111111");
        fhirMedicationStatementRange.setStartDate(weekAgo);
        fhirMedicationStatementRange.setEndDate(now);

        // built fhir patient
        Patient builtPatient = new Patient();

        // created fhir patient
        FhirDatabaseEntity fhirPatient = new FhirDatabaseEntity();
        fhirPatient.setLogicalId(UUID.randomUUID());

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);
        when(fhirResource.createEntity(eq(builtPatient), eq(ResourceType.Patient.name()),
                eq("patient"))).thenReturn(fhirPatient);
        when(groupRepository.findByCode(eq(fhirMedicationStatementRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirMedicationStatementRange.getIdentifier())))
                .thenReturn(identifiers);
        when(medicationService.deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirMedicationStatementRange.getStartDate()), eq(fhirMedicationStatementRange.getEndDate())))
                .thenReturn(1);
        when(patientService.buildPatient(eq(patient), eq(identifier))).thenReturn(builtPatient);
        when(Util.getFhirLink(eq(group), eq(identifier.getIdentifier()), eq(patient.getFhirLinks())))
                .thenReturn(patient.getFhirLinks().iterator().next());

        ServerResponse serverResponse = apiMedicationService.importMedication(fhirMedicationStatementRange);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct added success message, got '"
                + serverResponse.getSuccessMessage() + "'", !serverResponse.getSuccessMessage().contains("added 1"));
        Assert.assertTrue("Should have correct deleted success message, got '"
                + serverResponse.getSuccessMessage() + "'", serverResponse.getSuccessMessage().contains("deleted 1"));

        verify(fhirLinkService, times(0)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(medicationService, times(0)).add(any(FhirMedicationStatement.class),
                eq(patient.getFhirLinks().iterator().next()));
        verify(medicationService, times(1)).deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirMedicationStatementRange.getStartDate()), eq(fhirMedicationStatementRange.getEndDate()));
    }


    @Test
    public void testImportMedication_patientFromAnotherGroup_should_fail() throws Exception {
        // auth
        Group group = TestUtils.createGroup("ImporterGroup");
        Role staffRole = TestUtils.createRole(RoleName.IMPORTER);
        User staff = TestUtils.createUser("testImporter");
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

        TestUtils.createFhirLink(patient, identifier, patientGroup);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirMedicationStatementRange
        FhirMedicationStatementRange fhirMedicationStatementRange = new FhirMedicationStatementRange();
        fhirMedicationStatementRange.setGroupCode("DSF01");
        fhirMedicationStatementRange.setIdentifier("1111111111");
        fhirMedicationStatementRange.setStartDate(weekAgo);
        fhirMedicationStatementRange.setEndDate(now);
        fhirMedicationStatementRange.setMedications(new ArrayList<>());

        // FhirMedication to insert
        FhirMedicationStatement fhirMedicationStatement = new FhirMedicationStatement();
        fhirMedicationStatement.setName("medicationName");
        fhirMedicationStatement.setDose("medicationDose");
        fhirMedicationStatementRange.getMedications().add(fhirMedicationStatement);

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);

        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(groupRepository.findByCode(eq(fhirMedicationStatementRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirMedicationStatementRange.getIdentifier())))
                .thenReturn(identifiers);
        when(medicationService.deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirMedicationStatementRange.getStartDate()), eq(fhirMedicationStatementRange.getEndDate())))
                .thenReturn(1);

        ServerResponse serverResponse = apiMedicationService.importMedication(fhirMedicationStatementRange);

        Assert.assertTrue(
                "Should fail, got '" + serverResponse.getErrorMessage() + "'", !serverResponse.isSuccess());
        Assert.assertTrue("Should have correct error message, got '"
                        + serverResponse.getErrorMessage() + "'",
                serverResponse.getErrorMessage().contains("patient not a member of imported group"));

        verify(fhirLinkService, times(0)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(medicationService, times(0)).add(eq(fhirMedicationStatement),
                eq(patient.getFhirLinks().iterator().next()));
        verify(medicationService, times(0)).deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirMedicationStatementRange.getStartDate()), eq(fhirMedicationStatementRange.getEndDate()));
    }


    @Test
    public void testImportMedication_patientOutsideImporterGroup_shouldFail() throws Exception {
        // auth
        Group group = TestUtils.createGroup("ImporterGroup");
        Role staffRole = TestUtils.createRole(RoleName.IMPORTER);
        User staff = TestUtils.createUser("testImporter");
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

        TestUtils.createFhirLink(patient, identifier, patientGroup);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirMedicationStatementRange
        FhirMedicationStatementRange fhirMedicationStatementRange = new FhirMedicationStatementRange();
        fhirMedicationStatementRange.setGroupCode("DSF01");
        fhirMedicationStatementRange.setIdentifier("1111111111");
        fhirMedicationStatementRange.setStartDate(weekAgo);
        fhirMedicationStatementRange.setEndDate(now);
        fhirMedicationStatementRange.setMedications(new ArrayList<>());

        // FhirMedication to insert
        FhirMedicationStatement fhirMedicationStatement = new FhirMedicationStatement();
        fhirMedicationStatement.setName("medicationName");
        fhirMedicationStatement.setDose("medicationDose");
        fhirMedicationStatementRange.getMedications().add(fhirMedicationStatement);

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(false);

        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(patientGroup)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(groupRepository.findByCode(eq(fhirMedicationStatementRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirMedicationStatementRange.getIdentifier())))
                .thenReturn(identifiers);
        when(medicationService.deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirMedicationStatementRange.getStartDate()), eq(fhirMedicationStatementRange.getEndDate())))
                .thenReturn(1);

        ServerResponse serverResponse = apiMedicationService.importMedication(fhirMedicationStatementRange);

        Assert.assertTrue(
                "Should fail, got '" + serverResponse.getErrorMessage() + "'", !serverResponse.isSuccess());
        Assert.assertTrue("Should have correct error message, got '"
                + serverResponse.getErrorMessage() + "'", serverResponse.getErrorMessage().contains("Forbidden"));

        verify(fhirLinkService, times(0)).createFhirLink(eq(patient), eq(identifier),
                eq(patientGroup));
        verify(medicationService, times(0)).add(eq(fhirMedicationStatement),
                eq(patient.getFhirLinks().iterator().next()));
        verify(medicationService, times(0)).deleteBySubjectIdAndDateRange(any(UUID.class),
                eq(fhirMedicationStatementRange.getStartDate()), eq(fhirMedicationStatementRange.getEndDate()));
    }

}
