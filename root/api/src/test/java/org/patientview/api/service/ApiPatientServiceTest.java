package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.builder.PatientBuilder;
import org.patientview.api.service.impl.ApiPatientServiceImpl;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPatient;
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
import org.patientview.persistence.util.DataUtils;
import org.patientview.service.PatientService;
import org.patientview.test.util.TestUtils;
import org.patientview.util.Util;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for ApiPatientService, used for reading and writing patient record in FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 03/03/2015
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Util.class, DataUtils.class})
public class ApiPatientServiceTest {
    
    @InjectMocks
    ApiPatientService apiPatientService = new ApiPatientServiceImpl();

    @Mock
    private FhirLinkRepository fhirLinkRepository;

    @Mock
    private FhirResource fhirResource;

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
        PowerMockito.mockStatic(DataUtils.class);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testImportPatient_newPatient() throws Exception {
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
        FhirDatabaseEntity fhirDatabaseEntity = new FhirDatabaseEntity();
        fhirDatabaseEntity.setLogicalId(UUID.randomUUID());

        // data to import
        FhirPatient fhirPatient = new FhirPatient();
        fhirPatient.setForename("newForename");
        fhirPatient.setSurname("newSurname");
        fhirPatient.setGroupCode("RENALB");
        fhirPatient.setIdentifier(identifier.getIdentifier());

        when(fhirResource.createEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"))).thenReturn(fhirDatabaseEntity);
        when(groupRepository.findByCode(eq(fhirPatient.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirPatient.getIdentifier()))).thenReturn(identifiers);

        ServerResponse serverResponse = apiPatientService.importPatient(fhirPatient);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertEquals("Should have created message", "created", serverResponse.getSuccessMessage());

        verify(fhirResource, times(1)).createEntity(any(Patient.class), eq(ResourceType.Patient.name()), eq("patient"));
        verify(userRepository, times(1)).save(eq(patient));
    }

    @Test
    public void testImportPatient_updatePatient() throws Exception {
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

        // fhir links
        FhirLink fhirLink = TestUtils.createFhirLink(patient, identifier);
        patient.setFhirLinks(new HashSet<FhirLink>());
        patient.getFhirLinks().add(fhirLink);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // created fhir patient database entry
        FhirDatabaseEntity fhirDatabaseEntity = new FhirDatabaseEntity();
        fhirDatabaseEntity.setLogicalId(fhirLink.getResourceId());
        fhirDatabaseEntity.setVersionId(UUID.randomUUID());

        // data to import
        FhirPatient fhirPatient = new FhirPatient();
        fhirPatient.setForename("newForename");
        fhirPatient.setSurname("newSurname");
        fhirPatient.setGroupCode("RENALB");
        fhirPatient.setIdentifier(identifier.getIdentifier());

        // existing Patient
        Patient currentPatient = new Patient();

        when(DataUtils.getResource(any(JSONObject.class))).thenReturn(currentPatient);
        when(fhirResource.updateEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"), eq(fhirDatabaseEntity.getLogicalId()))).thenReturn(fhirDatabaseEntity);
        when(groupRepository.findByCode(eq(fhirPatient.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirPatient.getIdentifier()))).thenReturn(identifiers);
        when(Util.getFhirLink(eq(group), eq(identifier.getIdentifier()), eq(patient.getFhirLinks())))
                .thenReturn(patient.getFhirLinks().iterator().next());

        ServerResponse serverResponse = apiPatientService.importPatient(fhirPatient);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertEquals("Should have updated message", "updated", serverResponse.getSuccessMessage());

        verify(fhirResource, times(1)).updateEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"), eq(fhirDatabaseEntity.getLogicalId()));
        verify(fhirLinkRepository, times(1)).save(eq(fhirLink));
    }

    @Test
    public void testImportPatient_updatePatientMissingFhirPatient() throws Exception {
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

        // fhir links
        FhirLink fhirLink = TestUtils.createFhirLink(patient, identifier);
        patient.setFhirLinks(new HashSet<FhirLink>());
        patient.getFhirLinks().add(fhirLink);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // created fhir patient database entry
        FhirDatabaseEntity fhirDatabaseEntity = new FhirDatabaseEntity();
        fhirDatabaseEntity.setLogicalId(fhirLink.getResourceId());
        fhirDatabaseEntity.setVersionId(UUID.randomUUID());

        // data to import
        FhirPatient fhirPatient = new FhirPatient();
        fhirPatient.setForename("newForename");
        fhirPatient.setSurname("newSurname");
        fhirPatient.setGroupCode("RENALB");
        fhirPatient.setIdentifier(identifier.getIdentifier());

        when(fhirResource.createEntity(any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"))).thenReturn(fhirDatabaseEntity);
        when(groupRepository.findByCode(eq(fhirPatient.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirPatient.getIdentifier()))).thenReturn(identifiers);
        when(Util.getFhirLink(eq(group), eq(identifier.getIdentifier()), eq(patient.getFhirLinks())))
                .thenReturn(patient.getFhirLinks().iterator().next());

        ServerResponse serverResponse = apiPatientService.importPatient(fhirPatient);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertEquals("Should have updated message", "updated", serverResponse.getSuccessMessage());

        verify(fhirResource, times(1)).createEntity(any(Patient.class), eq(ResourceType.Patient.name()), eq("patient"));
        verify(fhirLinkRepository, times(1)).save(eq(fhirLink));
    }

    @Test
    public void testUpdate() throws Exception {

        String resourceId = "d52847eb-c2c7-4015-ba6c-952962536287";
        String versionId = "31d2f326-230a-4ce0-879b-443154a4d9e6";

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN_API);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        // patient
        User patient = TestUtils.createUser("testUser");
        patient.getGroupRoles().add(TestUtils.createGroupRole(TestUtils.createRole(RoleName.PATIENT), group, patient));
        patient.setFhirLinks(new HashSet<FhirLink>());
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(patient);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(UUID.fromString(resourceId));
        fhirLink.setActive(true);
        patient.getFhirLinks().add(fhirLink);

        // original patient object
        FhirPatient fhirPatient = new FhirPatient();
        fhirPatient.setForename("forename");
        fhirPatient.setSurname("surname");

        JSONObject content = new JSONObject();
        content.put("resourceType","Patient");

        JSONObject link = new JSONObject();
        link.put("href", "http://www.patientview.org/patient/" + versionId);

        JSONArray links = new JSONArray();
        links.put(link);

        JSONObject resource = new JSONObject();
        resource.put("link", links);
        resource.put("id", resourceId);
        resource.put("content", content);

        JSONArray resultArray = new JSONArray();
        resultArray.put(resource);

        JSONObject patientJson = new JSONObject();
        patientJson.put("entry", resultArray);

        FhirDatabaseEntity entity = new FhirDatabaseEntity(patientJson.toString(), ResourceType.Patient.name());
        entity.setLogicalId(UUID.randomUUID());

        when(DataUtils.getResource(any(JSONObject.class))).thenReturn(new Patient());
        when(userRepository.findById(Matchers.eq(patient.getId()))).thenReturn(Optional.of(patient));
        when(groupRepository.findById(Matchers.eq(group.getId()))).thenReturn(Optional.of(group));
        when(fhirResource.getResource(UUID.fromString(resourceId), ResourceType.Patient)).thenReturn(patientJson);
        when(fhirResource.updateEntity(
                any(Patient.class), eq(ResourceType.Patient.name()),
                eq("patient"), eq(fhirLink.getResourceId()))).thenReturn(entity);

        apiPatientService.update(patient.getId(), group.getId(), fhirPatient);
    }
}
