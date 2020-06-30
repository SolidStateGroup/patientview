package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.PatientManagementServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.PatientManagement;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.NonTestObservationTypes;
import org.patientview.persistence.model.enums.PatientManagementObservationTypes;
import org.patientview.persistence.model.enums.PractitionerRoles;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.ConditionService;
import org.patientview.service.EncounterService;
import org.patientview.service.FhirLinkService;
import org.patientview.service.ObservationService;
import org.patientview.service.OrganizationService;
import org.patientview.service.PractitionerService;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
    private EncounterService encounterService;

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

    @Mock
    private ObservationService observationService;

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private PatientManagementService patientManagementService = new PatientManagementServiceImpl();

    @Mock
    private PractitionerService practitionerService;

    @Mock
    private Properties properties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testImportPatientManagement() throws Exception {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.IMPORTER);
        User user = TestUtils.createUser("testUser");
        user.getGroupRoles().add(TestUtils.createGroupRole(role, group, user));
        TestUtils.authenticateTest(user, user.getGroupRoles());

        Date now = new Date();
        UUID organizationUuid = UUID.randomUUID();

        // code (diagnosis)
        Code code = TestUtils.createCode("Crohn's Disease");
        code.setCodeType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_TYPE), DiagnosisTypes.DIAGNOSIS.toString()));
        code.setCode("CD");

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
        patientManagement.setCondition(new FhirCondition());
        patientManagement.getCondition().setDate(now);
        patientManagement.getCondition().setCode(code.getCode());

        // patient details
        patientManagement.setPatient(new FhirPatient());
        patientManagement.getPatient().setPostcode("AB1 2CD");
        patientManagement.getPatient().setGender("Male");

        // encounter (surgery) details
        FhirEncounter fhirEncounter = new FhirEncounter();
        patientManagement.setEncounters(new ArrayList<FhirEncounter>());
        patientManagement.getEncounters().add(fhirEncounter);

        // required observations
        patientManagement.setObservations(new ArrayList<FhirObservation>());

        List<PatientManagementObservationTypes> requiredObservationTypes = new ArrayList<>();
        requiredObservationTypes.add(PatientManagementObservationTypes.WEIGHT);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_SMOKINGSTATUS);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSLOCATION);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSPROXIMALTERMINALILEUM);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSPERIANAL);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSBEHAVIOUR);

        for (PatientManagementObservationTypes type : requiredObservationTypes) {
            FhirObservation observation = new FhirObservation();
            observation.setName(type.toString());
            observation.setValue("00");
            patientManagement.getObservations().add(observation);
        }

        List<UUID> existingObservationUuids = new ArrayList<>();
        existingObservationUuids.add(UUID.randomUUID());

        // practitioners (ibd nurse, named consultant)
        FhirPractitioner fhirPractitioner = new FhirPractitioner();
        fhirPractitioner.setRole(PractitionerRoles.IBD_NURSE.toString());
        fhirPractitioner.setName("nurse name");
        patientManagement.setPractitioners(new ArrayList<FhirPractitioner>());
        patientManagement.getPractitioners().add(fhirPractitioner);
        UUID practitionerUuid = UUID.randomUUID();

        // import related fields
        patientManagement.setGroupCode(group.getCode());
        patientManagement.setIdentifier(identifier.getIdentifier());

        when(apiPatientService.get(eq(fhirLink.getResourceId()))).thenReturn(fhirPatient);
        when(codeRepository.findOneByCode(eq(code.getCode()))).thenReturn(code);
        when(fhirLinkRepository.findByUserAndGroupAndIdentifier(eq(patient), eq(group), eq(identifier)))
                .thenReturn(new ArrayList<>(patient.getFhirLinks()));
        when(fhirResource.getLogicalIdsBySubjectIdAndNames(eq("observation"),
                eq(patientFhirDatabaseEntity.getLogicalId()), any(List.class))).thenReturn(existingObservationUuids);
        when(fhirResource.updateEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"), eq(patientFhirDatabaseEntity.getLogicalId()))).thenReturn(patientFhirDatabaseEntity);
        when(groupRepository.existsById(eq(group.getId()))).thenReturn(true);
        when(groupRepository.findByCode(eq(patientManagement.getGroupCode()))).thenReturn(group);
        when(identifierRepository.existsById(eq(identifier.getId()))).thenReturn(true);
        when(identifierRepository.findByValue(eq(patientManagement.getIdentifier())))
                .thenReturn(new ArrayList<>(patient.getIdentifiers()));
        when(organizationService.add(eq(group))).thenReturn(organizationUuid);
        when(practitionerService.add(eq(fhirPractitioner))).thenReturn(practitionerUuid);
        when(userRepository.existsById(eq(patient.getId()))).thenReturn(true);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);

        // import
        ServerResponse serverResponse = patientManagementService.importPatientManagement(patientManagement);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertEquals("Should have saved message", "saved", serverResponse.getSuccessMessage());

        verify(apiPatientService, times(2)).get(eq(patient.getFhirLinks().iterator().next().getResourceId()));
        verify(codeRepository, times(1)).findOneByCode(eq(code.getCode()));
        verify(conditionService, times(1)).add(
                eq(patientManagement.getCondition()), eq(patient.getFhirLinks().iterator().next()));
        verify(fhirResource, times(1)).getLogicalIdsBySubjectIdAndNames(eq("observation"),
                eq(patientFhirDatabaseEntity.getLogicalId()), any(List.class));
        verify(fhirResource, times(2)).updateEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"), eq(patientFhirDatabaseEntity.getLogicalId()));
        verify(fhirLinkRepository, times(1)).save(eq(fhirLink));
        verify(encounterService, times(1)).add(eq(fhirEncounter), eq(fhirLink), eq(organizationUuid));
        verify(encounterService, times(1)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(EncounterTypes.SURGERY));
        verify(observationService, times(6)).add(any(FhirObservation.class), eq(fhirLink));
        verify(observationService, times(1)).deleteObservations(eq(existingObservationUuids));
        verify(organizationService, times(1)).add(eq(group));
    }

    @Test
    public void testSave() throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.getGroupRoles().add(TestUtils.createGroupRole(role, group, user));
        TestUtils.authenticateTest(user, user.getGroupRoles());

        Date now = new Date();
        UUID organizationUuid = UUID.randomUUID();

        // code (diagnosis)
        Code code = TestUtils.createCode("Crohn's Disease");
        code.setCodeType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_TYPE), DiagnosisTypes.DIAGNOSIS.toString()));
        code.setCode("CD");

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
        patientManagement.setCondition(new FhirCondition());
        patientManagement.getCondition().setDate(now);
        patientManagement.getCondition().setCode(code.getCode());

        // patient details
        patientManagement.setPatient(new FhirPatient());
        patientManagement.getPatient().setPostcode("AB1 2CD");

        // encounter (surgery) details
        FhirEncounter fhirEncounter = new FhirEncounter();
        patientManagement.setEncounters(new ArrayList<FhirEncounter>());
        patientManagement.getEncounters().add(fhirEncounter);

        // observation (selects, text fields) details
        FhirObservation fhirObservation = new FhirObservation();
        fhirObservation.setName(NonTestObservationTypes.IBD_ALLERGYSUBSTANCE.toString());
        fhirObservation.setValue("1");
        patientManagement.setObservations(new ArrayList<FhirObservation>());
        patientManagement.getObservations().add(fhirObservation);

        List<UUID> existingObservationUuids = new ArrayList<>();
        existingObservationUuids.add(UUID.randomUUID());

        // practitioners (ibd nurse, named consultant)
        FhirPractitioner fhirPractitioner = new FhirPractitioner();
        fhirPractitioner.setRole(PractitionerRoles.IBD_NURSE.toString());
        fhirPractitioner.setName("nurse name");
        patientManagement.setPractitioners(new ArrayList<FhirPractitioner>());
        patientManagement.getPractitioners().add(fhirPractitioner);
        UUID practitionerUuid = UUID.randomUUID();

        when(apiPatientService.get(eq(fhirLink.getResourceId()))).thenReturn(fhirPatient);
        when(fhirLinkRepository.findByUserAndGroupAndIdentifier(eq(patient), eq(group), eq(identifier)))
                .thenReturn(new ArrayList<>(patient.getFhirLinks()));
        when(fhirResource.getLogicalIdsBySubjectIdAndNames(eq("observation"),
                eq(patientFhirDatabaseEntity.getLogicalId()), any(List.class))).thenReturn(existingObservationUuids);
        when(fhirResource.updateEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"), eq(patientFhirDatabaseEntity.getLogicalId()))).thenReturn(patientFhirDatabaseEntity);
        when(groupRepository.existsById(eq(group.getId()))).thenReturn(true);
        when(identifierRepository.existsById(eq(identifier.getId()))).thenReturn(true);
        when(organizationService.add(eq(group))).thenReturn(organizationUuid);
        when(practitionerService.add(eq(fhirPractitioner))).thenReturn(practitionerUuid);
        when(userRepository.existsById(eq(patient.getId()))).thenReturn(true);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);

        // save
        patientManagementService.save(patient, group, identifier, patientManagement);

        verify(apiPatientService, times(2)).get(eq(patient.getFhirLinks().iterator().next().getResourceId()));
        verify(conditionService, times(1)).add(
                eq(patientManagement.getCondition()), eq(patient.getFhirLinks().iterator().next()));
        verify(fhirResource, times(1)).getLogicalIdsBySubjectIdAndNames(eq("observation"),
                eq(patientFhirDatabaseEntity.getLogicalId()), any(List.class));
        verify(fhirResource, times(2)).updateEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"), eq(patientFhirDatabaseEntity.getLogicalId()));
        verify(fhirLinkRepository, times(1)).save(eq(fhirLink));
        verify(encounterService, times(1)).add(eq(fhirEncounter), eq(fhirLink), eq(organizationUuid));
        verify(encounterService, times(1)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(EncounterTypes.SURGERY));
        verify(observationService, times(1)).add(eq(fhirObservation), eq(fhirLink));
        verify(observationService, times(1)).deleteObservations(eq(existingObservationUuids));
        verify(organizationService, times(1)).add(eq(group));
    }

    @Test
    public void testSaveSurgeries()
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.getGroupRoles().add(TestUtils.createGroupRole(role, group, user));
        TestUtils.authenticateTest(user, user.getGroupRoles());

        Date now = new Date();
        UUID organizationUuid = UUID.randomUUID();

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

        // encounter (surgery) details
        FhirEncounter fhirEncounter = new FhirEncounter();
        patientManagement.setEncounters(new ArrayList<FhirEncounter>());
        patientManagement.getEncounters().add(fhirEncounter);

        when(fhirLinkRepository.findByUserAndGroupAndIdentifier(eq(patient), eq(group), eq(identifier)))
                .thenReturn(new ArrayList<>(patient.getFhirLinks()));
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(identifierRepository.findById(eq(identifier.getId()))).thenReturn(Optional.of(identifier));
        when(organizationService.add(eq(group))).thenReturn(organizationUuid);
        when(userRepository.findById(eq(patient.getId()))).thenReturn(Optional.of(patient));
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);

        // save
        patientManagementService.saveSurgeries(patient.getId(), group.getId(), identifier.getId(), patientManagement);

        verify(encounterService, times(1)).add(eq(fhirEncounter), eq(fhirLink), eq(organizationUuid));
        verify(encounterService, times(1)).deleteBySubjectIdAndType(
                eq(fhirLink.getResourceId()), eq(EncounterTypes.SURGERY));
        verify(organizationService, times(1)).add(eq(group));
    }

    @Test
    public void testValidate() throws VerificationException {
        // current user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        user.getGroupRoles().add(TestUtils.createGroupRole(role, group, user));
        TestUtils.authenticateTest(user, user.getGroupRoles());

        Date now = new Date();

        Code code = TestUtils.createCode("Crohn's Disease");
        code.setCodeType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_TYPE), DiagnosisTypes.DIAGNOSIS.toString()));
        code.setCode("CD");

        PatientManagement patientManagement = new PatientManagement();
        patientManagement.setCondition(new FhirCondition());
        patientManagement.getCondition().setDate(now);
        patientManagement.getCondition().setCode(code.getCode());

        // required observations
        patientManagement.setObservations(new ArrayList<FhirObservation>());

        List<PatientManagementObservationTypes> requiredObservationTypes = new ArrayList<>();
        requiredObservationTypes.add(PatientManagementObservationTypes.WEIGHT);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_SMOKINGSTATUS);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSLOCATION);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSPROXIMALTERMINALILEUM);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSPERIANAL);
        requiredObservationTypes.add(PatientManagementObservationTypes.IBD_CROHNSBEHAVIOUR);

        for (PatientManagementObservationTypes type : requiredObservationTypes) {
            FhirObservation observation = new FhirObservation();
            observation.setName(type.toString());
            observation.setValue("00");
            patientManagement.getObservations().add(observation);
        }

        // required gender & postcode
        patientManagement.setPatient(new FhirPatient());
        patientManagement.getPatient().setPostcode("abc123");
        patientManagement.getPatient().setGender("Male");

        when(codeRepository.findOneByCode(eq(patientManagement.getCondition().getCode()))).thenReturn(code);

        patientManagementService.validate(patientManagement);
    }
}
