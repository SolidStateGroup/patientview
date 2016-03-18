package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.PatientManagementServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.PatientManagement;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.ConditionService;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/03/2016
 */
public class PatientManagementServiceTest {

    @Mock
    private ApiPatientService apiPatientService;

    @Mock
    private CodeRepository codeRepository;

    @Mock
    private ConditionService conditionService;

    @Mock
    private FhirLinkRepository fhirLinkRepository;

    @Mock
    private FhirLinkService fhirLinkService;

    @Mock
    private FhirResource fhirResource;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private IdentifierRepository identifierRepository;

    @InjectMocks
    private PatientManagementService patientManagementService = new PatientManagementServiceImpl();

    @Mock
    private Properties properties;

    @Mock
    private UserRepository userRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testSave() throws ResourceNotFoundException, FhirResourceException {
        Date now = new Date();

        // code (diagnosis)
        Code code = TestUtils.createCode("Crohn's Disease");
        code.setCodeType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_TYPE), DiagnosisTypes.DIAGNOSIS.toString()));
        code.setCode("CD");

        // group
        Group group = TestUtils.createGroup("testGroup");

        // user
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
        patient.getIdentifiers().add(identifier);

        // fhir links
        FhirLink fhirLink = TestUtils.createFhirLink(patient, identifier);
        patient.setFhirLinks(new HashSet<FhirLink>());
        patient.getFhirLinks().add(fhirLink);

        // FHIR Patient, updated as fhirlink creation creates new patient anyway
        Patient fhirPatient = new Patient();
        FhirDatabaseEntity patientFhirDatabaseEntity = new FhirDatabaseEntity();
        patientFhirDatabaseEntity.setLogicalId(fhirLink.getResourceId());
        patientFhirDatabaseEntity.setVersionId(UUID.randomUUID());

        // PatientManagement
        PatientManagement patientManagement = new PatientManagement();

        // diagnosis details
        patientManagement.setFhirCondition(new FhirCondition());
        patientManagement.getFhirCondition().setDate(now);
        patientManagement.getFhirCondition().setCode(code.getCode());

        // patient details
        patientManagement.setFhirPatient(new FhirPatient());
        patientManagement.getFhirPatient().setPostcode("AB1 2CD");

        when(apiPatientService.get(eq(fhirLink.getResourceId()))).thenReturn(fhirPatient);
        when(fhirLinkRepository.findByUserAndGroupAndIdentifier(eq(patient), eq(group), eq(identifier)))
                .thenReturn(new ArrayList<>(patient.getFhirLinks()));
        when(fhirResource.updateEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"), eq(patientFhirDatabaseEntity.getLogicalId()))).thenReturn(patientFhirDatabaseEntity);
        when(groupRepository.exists(eq(group.getId()))).thenReturn(true);
        when(identifierRepository.exists(eq(identifier.getId()))).thenReturn(true);
        when(userRepository.exists(eq(patient.getId()))).thenReturn(true);

        // save
        patientManagementService.save(patient, group, identifier, patientManagement);

        verify(apiPatientService, times(1)).get(eq(patient.getFhirLinks().iterator().next().getResourceId()));
        verify(conditionService, times(1)).add(
                eq(patientManagement.getFhirCondition()), eq(patient.getFhirLinks().iterator().next()));
        verify(fhirResource, times(1)).updateEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"), eq(patientFhirDatabaseEntity.getLogicalId()));
        verify(fhirLinkRepository, times(1)).save(eq(fhirLink));
    }

    @Test
    public void testValidate() throws VerificationException {
        Date now = new Date();

        Code code = TestUtils.createCode("Crohn's Disease");
        code.setCodeType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_TYPE), DiagnosisTypes.DIAGNOSIS.toString()));
        code.setCode("CD");

        PatientManagement patientManagement = new PatientManagement();
        patientManagement.setFhirCondition(new FhirCondition());
        patientManagement.getFhirCondition().setDate(now);
        patientManagement.getFhirCondition().setCode(code.getCode());

        when(codeRepository.findOneByCode(eq(patientManagement.getFhirCondition().getCode()))).thenReturn(code);

        patientManagementService.validate(patientManagement);
    }
}
