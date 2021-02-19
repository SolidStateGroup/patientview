package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.ClinicalDataServiceImpl;
import org.patientview.persistence.model.FhirClinicalData;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.ConditionService;
import org.patientview.service.EncounterService;
import org.patientview.service.FhirLinkService;
import org.patientview.service.OrganizationService;
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
 * Created on 09/03/2016
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class ClinicalDataServiceTest {

    @InjectMocks
    ClinicalDataService clinicalDataService = new ClinicalDataServiceImpl();

    @Mock
    CodeRepository codeRepository;

    @Mock
    ConditionService conditionService;

    @Mock
    EncounterService encounterService;

    @Mock
    FhirLinkService fhirLinkService;

    @Mock
    FhirResource fhirResource;

    @Mock
    GroupRepository groupRepository;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    LookupRepository lookupRepository;

    @Mock
    OrganizationService organizationService;

    @Mock
    UserService userService;

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
    public void testImportClinicalData() throws Exception {
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

        // FhirLink
        TestUtils.createFhirLink(patient, identifier, group);
        FhirLink fhirLink = patient.getFhirLinks().iterator().next();

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // parent FhirClinicalData object containing treatment and diagnoses
        FhirClinicalData fhirClinicalData = new FhirClinicalData();
        fhirClinicalData.setGroupCode("DSF01");
        fhirClinicalData.setIdentifier("1111111111");

        // treatment Encounter
        FhirEncounter treatment = new FhirEncounter();
        treatment.setStatus("transfusion");
        fhirClinicalData.setTreatments(new ArrayList<FhirEncounter>());
        FhirEncounter treatment2 = new FhirEncounter();
        treatment2.setStatus("removal");
        fhirClinicalData.setTreatments(new ArrayList<FhirEncounter>());

        fhirClinicalData.getTreatments().add(treatment);
        fhirClinicalData.getTreatments().add(treatment2);

        // diagnosis Condition
        FhirCondition diagnosis = new FhirCondition();
        diagnosis.setCode("00");
        diagnosis.setDate(new Date());

        // other diagnosis Condition
        FhirCondition otherDiagnosis = new FhirCondition();
        otherDiagnosis.setCode("another diagnosis");
        otherDiagnosis.setDate(new Date());

        fhirClinicalData.setDiagnoses(new ArrayList<FhirCondition>());
        fhirClinicalData.getDiagnoses().add(diagnosis);
        fhirClinicalData.getDiagnoses().add(otherDiagnosis);

        // from Organization create/update
        UUID organizationUuid = UUID.randomUUID();

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);

        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group))).thenReturn(fhirLink);
        when(groupRepository.findByCode(eq(fhirClinicalData.getGroupCode()))).thenReturn(group);
        when(organizationService.add(eq(group))).thenReturn(organizationUuid);
        when(identifierRepository.findByValue(eq(fhirClinicalData.getIdentifier())))
                .thenReturn(identifiers);

        ServerResponse serverResponse = clinicalDataService.importClinicalData(fhirClinicalData);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct added success message, got '" + serverResponse.getSuccessMessage()
                + "'", serverResponse.getSuccessMessage().contains("saved 2 treatments"));
        Assert.assertTrue("Should have correct added success message, got '" + serverResponse.getSuccessMessage()
                + "'", serverResponse.getSuccessMessage().contains("saved 2 diagnoses"));

        verify(conditionService, times(1)).add(eq(fhirClinicalData.getDiagnoses().get(0)), eq(fhirLink));
        verify(conditionService, times(1)).add(eq(fhirClinicalData.getDiagnoses().get(1)), eq(fhirLink));
        verify(conditionService, times(1)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(DiagnosisTypes.DIAGNOSIS));
        verify(encounterService, times(1)).add(
                eq(treatment), eq(patient.getFhirLinks().iterator().next()), eq(organizationUuid));
        verify(encounterService, times(1)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(EncounterTypes.TREATMENT));
        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(organizationService, times(1)).add(eq(group));
    }

    @Test
    public void testImportClinicalData_onlyErase() throws Exception {
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

        // parent FhirClinicalData object containing treatment and diagnoses
        FhirClinicalData fhirClinicalData = new FhirClinicalData();
        fhirClinicalData.setGroupCode("DSF01");
        fhirClinicalData.setIdentifier("1111111111");

        fhirClinicalData.setDiagnoses(new ArrayList<FhirCondition>());
        fhirClinicalData.setTreatments(new ArrayList<FhirEncounter>());

        // from Organization create/update
        UUID organizationUuid = UUID.randomUUID();

        FhirLink fhirLink = patient.getFhirLinks().iterator().next();

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);

        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group))).thenReturn(fhirLink);
        when(groupRepository.findByCode(eq(fhirClinicalData.getGroupCode()))).thenReturn(group);
        when(organizationService.add(eq(group))).thenReturn(organizationUuid);
        when(identifierRepository.findByValue(eq(fhirClinicalData.getIdentifier())))
                .thenReturn(identifiers);

        ServerResponse serverResponse = clinicalDataService.importClinicalData(fhirClinicalData);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct added success message, got '" + serverResponse.getSuccessMessage()
                + "'", serverResponse.getSuccessMessage().contains("removed treatments"));
        Assert.assertTrue("Should have correct added success message, got '" + serverResponse.getSuccessMessage()
                + "'", serverResponse.getSuccessMessage().contains("removed diagnoses"));

        verify(conditionService, times(0)).add(any(FhirCondition.class), eq(fhirLink));
        verify(conditionService, times(1)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(DiagnosisTypes.DIAGNOSIS));
        verify(encounterService, times(0)).add(
                any(FhirEncounter.class), eq(patient.getFhirLinks().iterator().next()), eq(organizationUuid));
        verify(encounterService, times(1)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(EncounterTypes.TREATMENT));
        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(organizationService, times(1)).add(eq(group));
    }


    @Test
    public void testImportClinicalData_patientFromAnotherGroup_shouldFail() throws Exception {
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

        // FhirLink
        TestUtils.createFhirLink(patient, identifier, patientGroup);
        FhirLink fhirLink = patient.getFhirLinks().iterator().next();

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // parent FhirClinicalData object containing treatment and diagnoses
        FhirClinicalData fhirClinicalData = new FhirClinicalData();
        fhirClinicalData.setGroupCode("DSF01");
        fhirClinicalData.setIdentifier("1111111111");

        // treatment Encounter
        FhirEncounter treatment = new FhirEncounter();
        treatment.setStatus("transfusion");
        fhirClinicalData.setTreatments(new ArrayList<>());
        FhirEncounter treatment2 = new FhirEncounter();
        treatment2.setStatus("removal");
        fhirClinicalData.setTreatments(new ArrayList<>());

        fhirClinicalData.getTreatments().add(treatment);
        fhirClinicalData.getTreatments().add(treatment2);

        // diagnosis Condition
        FhirCondition diagnosis = new FhirCondition();
        diagnosis.setCode("00");
        diagnosis.setDate(new Date());

        // other diagnosis Condition
        FhirCondition otherDiagnosis = new FhirCondition();
        otherDiagnosis.setCode("another diagnosis");
        otherDiagnosis.setDate(new Date());

        fhirClinicalData.setDiagnoses(new ArrayList<>());
        fhirClinicalData.getDiagnoses().add(diagnosis);
        fhirClinicalData.getDiagnoses().add(otherDiagnosis);

        // from Organization create/update
        UUID organizationUuid = UUID.randomUUID();

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(true);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group))).thenReturn(fhirLink);
        when(groupRepository.findByCode(eq(fhirClinicalData.getGroupCode()))).thenReturn(group);
        when(organizationService.add(eq(group))).thenReturn(organizationUuid);
        when(identifierRepository.findByValue(eq(fhirClinicalData.getIdentifier())))
                .thenReturn(identifiers);

        ServerResponse serverResponse = clinicalDataService.importClinicalData(fhirClinicalData);

        Assert.assertTrue(
                "Should fail, got '" + serverResponse.getErrorMessage() + "'", !serverResponse.isSuccess());
        Assert.assertTrue("Should have correct error message, got '"
                        + serverResponse.getErrorMessage() + "'",
                serverResponse.getErrorMessage().contains("patient not a member of imported group"));

        verify(conditionService, times(0)).add(eq(fhirClinicalData.getDiagnoses().get(0)), eq(fhirLink));
        verify(conditionService, times(0)).add(eq(fhirClinicalData.getDiagnoses().get(0)), eq(fhirLink));
        verify(conditionService, times(0)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(DiagnosisTypes.DIAGNOSIS));
        verify(encounterService, times(0)).add(
                eq(treatment), eq(patient.getFhirLinks().iterator().next()), eq(organizationUuid));
        verify(encounterService, times(0)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(EncounterTypes.TREATMENT));
        verify(fhirLinkService, times(0)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(organizationService, times(0)).add(eq(group));
    }

    @Test
    public void testImportClinicalData_patientOutsideImporterGroup_shouldFail() throws Exception {
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

        // FhirLink
        TestUtils.createFhirLink(patient, identifier, group);
        FhirLink fhirLink = patient.getFhirLinks().iterator().next();

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // parent FhirClinicalData object containing treatment and diagnoses
        FhirClinicalData fhirClinicalData = new FhirClinicalData();
        fhirClinicalData.setGroupCode("DSF01");
        fhirClinicalData.setIdentifier("1111111111");

        // treatment Encounter
        FhirEncounter treatment = new FhirEncounter();
        treatment.setStatus("transfusion");
        fhirClinicalData.setTreatments(new ArrayList<FhirEncounter>());
        FhirEncounter treatment2 = new FhirEncounter();
        treatment2.setStatus("removal");
        fhirClinicalData.setTreatments(new ArrayList<FhirEncounter>());

        fhirClinicalData.getTreatments().add(treatment);
        fhirClinicalData.getTreatments().add(treatment2);

        // diagnosis Condition
        FhirCondition diagnosis = new FhirCondition();
        diagnosis.setCode("00");
        diagnosis.setDate(new Date());

        // other diagnosis Condition
        FhirCondition otherDiagnosis = new FhirCondition();
        otherDiagnosis.setCode("another diagnosis");
        otherDiagnosis.setDate(new Date());

        fhirClinicalData.setDiagnoses(new ArrayList<FhirCondition>());
        fhirClinicalData.getDiagnoses().add(diagnosis);
        fhirClinicalData.getDiagnoses().add(otherDiagnosis);

        // from Organization create/update
        UUID organizationUuid = UUID.randomUUID();

        when(userService.currentUserSameUnitGroup(eq(patient), eq(RoleName.IMPORTER))).thenReturn(false);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group))).thenReturn(fhirLink);
        when(groupRepository.findByCode(eq(fhirClinicalData.getGroupCode()))).thenReturn(group);
        when(organizationService.add(eq(group))).thenReturn(organizationUuid);
        when(identifierRepository.findByValue(eq(fhirClinicalData.getIdentifier())))
                .thenReturn(identifiers);

        ServerResponse serverResponse = clinicalDataService.importClinicalData(fhirClinicalData);

        Assert.assertTrue(
                "Should fail, got '" + serverResponse.getErrorMessage() + "'", !serverResponse.isSuccess());
        Assert.assertTrue("Should have correct error message, got '"
                + serverResponse.getErrorMessage() + "'", serverResponse.getErrorMessage().contains("Forbidden"));

        verify(conditionService, times(0)).add(eq(fhirClinicalData.getDiagnoses().get(0)), eq(fhirLink));
        verify(conditionService, times(0)).add(eq(fhirClinicalData.getDiagnoses().get(0)), eq(fhirLink));
        verify(conditionService, times(0)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(DiagnosisTypes.DIAGNOSIS));
        verify(encounterService, times(0)).add(
                eq(treatment), eq(patient.getFhirLinks().iterator().next()), eq(organizationUuid));
        verify(encounterService, times(0)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(EncounterTypes.TREATMENT));
        verify(fhirLinkService, times(0)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(organizationService, times(0)).add(eq(group));
    }
}
