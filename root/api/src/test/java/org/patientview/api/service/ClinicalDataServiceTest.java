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
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.ConditionService;
import org.patientview.service.EncounterService;
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

    User creator;

    @InjectMocks
    ClinicalDataService clinicalDataService = new ClinicalDataServiceImpl();

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
    OrganizationService organizationService;

    @Mock
    UserRepository userRepository;

    private Date now;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
        PowerMockito.mockStatic(Util.class);
        this.now = new Date();
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

        TestUtils.createFhirLink(patient, identifier, group);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // parent FhirClinicalData object containing treatment and diagnoses
        FhirClinicalData fhirClinicalData = new FhirClinicalData();
        fhirClinicalData.setGroupCode("DSF01");
        fhirClinicalData.setIdentifier("1111111111");

        // treatment Encounter
        FhirEncounter treatment = new FhirEncounter();
        treatment.setStatus("transfusion");
        fhirClinicalData.setTreatment(treatment);

        // diagnosis Condition
        FhirCondition diagnosis = new FhirCondition();
        diagnosis.setCode("00");
        fhirClinicalData.setDiagnosis(diagnosis);

        // from Organization create/update
        UUID organizationUuid = UUID.randomUUID();

        FhirLink fhirLink = patient.getFhirLinks().iterator().next();

        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group))).thenReturn(fhirLink);
        when(groupRepository.findByCode(eq(fhirClinicalData.getGroupCode()))).thenReturn(group);
        when(organizationService.add(eq(group))).thenReturn(organizationUuid);
        when(identifierRepository.findByValue(eq(fhirClinicalData.getIdentifier())))
                .thenReturn(identifiers);

        ServerResponse serverResponse = clinicalDataService.importClinicalData(fhirClinicalData);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());

        verify(conditionService, times(1)).add(eq(fhirClinicalData.getDiagnosis()), eq(fhirLink));
        verify(conditionService, times(1)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(DiagnosisTypes.DIAGNOSIS_EDTA));
        verify(encounterService, times(1)).add(
                eq(treatment), eq(patient.getFhirLinks().iterator().next()),eq(organizationUuid));
        verify(encounterService, times(1)).deleteByUserAndType(eq(patient), eq(EncounterTypes.TREATMENT));
        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(organizationService, times(1)).add(eq(group));
    }
}
