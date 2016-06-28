package org.patientview.api.service;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.Condition;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Practitioner;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.ApiConditionServiceImpl;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.persistence.util.DataUtils;
import org.patientview.service.FhirLinkService;
import org.patientview.service.ObservationService;
import org.patientview.service.PatientService;
import org.patientview.service.PractitionerService;
import org.patientview.test.util.TestUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
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
 * Created on 02/12/2015
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(DataUtils.class)
public class ApiConditionServiceTest {

    @InjectMocks
    ApiConditionService apiConditionService = new ApiConditionServiceImpl();

    @Mock
    CodeService codeService;

    @Mock
    FhirLinkRepository fhirLinkRepository;

    @Mock
    FhirLinkService fhirLinkService;

    @Mock
    FhirResource fhirResource;

    @Mock
    GroupService groupService;

    @Mock
    LookupService lookupService;

    @Mock
    ObservationService observationService;

    @Mock
    PatientService patientService;

    @Mock
    PractitionerService practitionerService;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(DataUtils.class);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetStaffEntered() throws Exception {
        String code = "00";

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User staffUser = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, staffUser);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staffUser.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(staffUser, groupRoles);

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // patient identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");

        // staff entered results group
        Group staffEntered = TestUtils.createGroup("staffEntered");
        staffEntered.setCode(HiddenGroupCodes.STAFF_ENTERED.toString());

        // fhir link
        FhirLink fhirLink = TestUtils.createFhirLink(patient, identifier);
        fhirLink.setGroup(staffEntered);
        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.add(fhirLink);

        // returned conditions
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setTextSimple(code);
        Condition condition = new Condition();
        condition.setCode(codeableConcept);
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);

        when(fhirLinkRepository.findByUserAndGroupAndIdentifier(eq(patient), eq(staffEntered), eq(identifier)))
                .thenReturn(fhirLinks);
        when(groupService.findByCode(eq(HiddenGroupCodes.STAFF_ENTERED.toString()))).thenReturn(staffEntered);
        when(userService.get(eq(patient.getId()))).thenReturn(patient);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);
        when(fhirResource.findResourceByQuery(eq("SELECT content::varchar " + "FROM condition "
                + "WHERE content -> 'subject' ->> 'display' = '"
                + fhirLink.getResourceId() + "' "), eq(Condition.class))).thenReturn(conditions);

        List<FhirCondition> conditionsList = apiConditionService.getStaffEntered(patient.getId());

        verify(fhirResource, times(1)).findResourceByQuery(eq("SELECT content::varchar " + "FROM condition "
                + "WHERE content -> 'subject' ->> 'display' = '"
                + fhirLink.getResourceId() + "' "), eq(Condition.class));
        Assert.assertEquals("There should be 1 condition", 1, conditionsList.size());
    }

    @Test
    public void testStaffAddCondition() throws Exception {
        String code = "00";

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User staffUser = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, staffUser);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staffUser.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(staffUser, groupRoles);

        // staff UUID, used when getting Practitioner
        UUID staffUuid = UUID.randomUUID();
        List<UUID> staffUuids = new ArrayList<>();
        staffUuids.add(staffUuid);

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // patient identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // staff entered results group
        Group staffEntered = TestUtils.createGroup("staffEntered");
        staffEntered.setCode(HiddenGroupCodes.STAFF_ENTERED.toString());

        // code
        Lookup lookup = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_TYPE), CodeTypes.DIAGNOSIS.toString());
        Code codeEntity = TestUtils.createCode(code);
        List<Code> codes = new ArrayList<>();
        codes.add(codeEntity);

        // fhir link
        FhirLink fhirLink = TestUtils.createFhirLink(patient, identifier);
        fhirLink.setGroup(staffEntered);
        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.add(fhirLink);

        when(codeService.findAllByCodeAndType(eq(code), eq(lookup))).thenReturn(codes);
        when(fhirLinkRepository.findByUserAndGroupAndIdentifier(eq(patient), eq(staffEntered), eq(identifier)))
                .thenReturn(fhirLinks);
        when(groupService.findByCode(eq(HiddenGroupCodes.STAFF_ENTERED.toString()))).thenReturn(staffEntered);
        when(lookupService.findByTypeAndValue(eq(LookupTypes.CODE_TYPE), eq(CodeTypes.DIAGNOSIS.toString())))
                .thenReturn(lookup);
        when(practitionerService.getPractitionerLogicalUuidsByName(eq(staffUser.getId().toString())))
                .thenReturn(staffUuids);
        when(userService.get(eq(patient.getId()))).thenReturn(patient);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);

        apiConditionService.staffAddCondition(patient.getId(), code);

        verify(fhirResource, times(1)).createEntity(
                any(Condition.class), eq(ResourceType.Condition.name()), eq("condition"));
    }

    @Test
    public void testStaffAddCondition_newFhirLink() throws Exception {
        String code = "00";

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User staffUser = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, staffUser);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staffUser.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(staffUser, groupRoles);

        // staff UUID, used when getting Practitioner
        UUID staffUuid = UUID.randomUUID();
        List<UUID> staffUuids = new ArrayList<>();
        staffUuids.add(staffUuid);

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // patient identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // staff entered results group
        Group staffEntered = TestUtils.createGroup("staffEntered");
        staffEntered.setCode(HiddenGroupCodes.STAFF_ENTERED.toString());

        // code
        Lookup lookup = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_TYPE), CodeTypes.DIAGNOSIS.toString());
        Code codeEntity = TestUtils.createCode(code);
        List<Code> codes = new ArrayList<>();
        codes.add(codeEntity);

        // fhir patient
        FhirDatabaseEntity fhirPatient = new FhirDatabaseEntity();
        fhirPatient.setLogicalId(UUID.randomUUID());
        fhirPatient.setVersionId(UUID.randomUUID());

        // fhir link
        FhirLink fhirLink = TestUtils.createFhirLink(patient, identifier);
        fhirLink.setGroup(staffEntered);
        fhirLink.setResourceId(UUID.randomUUID());

        when(codeService.findAllByCodeAndType(eq(code), eq(lookup))).thenReturn(codes);
        when(fhirLinkRepository.findByUserAndGroupAndIdentifier(eq(patient), eq(staffEntered), eq(identifier)))
                .thenReturn(null);
        when(fhirLinkService.createFhirLink(eq(patient), eq(patient.getIdentifiers().iterator().next()),
                eq(staffEntered))).thenReturn(fhirLink);
        when(fhirResource.createEntity(any(Resource.class), eq(ResourceType.Patient.name()), eq("patient")))
                .thenReturn(fhirPatient);
        when(groupService.findByCode(eq(HiddenGroupCodes.STAFF_ENTERED.toString()))).thenReturn(staffEntered);
        when(lookupService.findByTypeAndValue(eq(LookupTypes.CODE_TYPE), eq(CodeTypes.DIAGNOSIS.toString())))
                .thenReturn(lookup);
        when(patientService.buildPatient(eq(patient), eq(identifier))).thenReturn(new Patient());
        when(practitionerService.getPractitionerLogicalUuidsByName(eq(staffUser.getId().toString())))
                .thenReturn(staffUuids);
        when(userService.get(eq(patient.getId()))).thenReturn(patient);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);

        apiConditionService.staffAddCondition(patient.getId(), code);

        verify(fhirResource, times(1)).createEntity(
                any(Condition.class), eq(ResourceType.Condition.name()), eq("condition"));

        // required in adding FHIR link
        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(patient.getIdentifiers().iterator().next()),
                eq(staffEntered));
    }

    @Test
    public void testStaffAddCondition_newFhirLinkNewPractitioner() throws Exception {
        String code = "00";

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User staffUser = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, staffUser);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staffUser.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(staffUser, groupRoles);

        // staff UUID, used when getting Practitioner
        UUID staffUuid = UUID.randomUUID();
        List<UUID> staffUuids = new ArrayList<>();
        staffUuids.add(staffUuid);

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // patient identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // staff entered results group
        Group staffEntered = TestUtils.createGroup("staffEntered");
        staffEntered.setCode(HiddenGroupCodes.STAFF_ENTERED.toString());

        // code
        Lookup lookup = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_TYPE), CodeTypes.DIAGNOSIS.toString());
        Code codeEntity = TestUtils.createCode(code);
        List<Code> codes = new ArrayList<>();
        codes.add(codeEntity);

        // fhir patient
        FhirDatabaseEntity fhirPatient = new FhirDatabaseEntity();
        fhirPatient.setLogicalId(UUID.randomUUID());
        fhirPatient.setVersionId(UUID.randomUUID());

        // fhir practitioner
        FhirDatabaseEntity fhirPractitioner = new FhirDatabaseEntity();
        fhirPractitioner.setLogicalId(UUID.randomUUID());
        fhirPractitioner.setVersionId(UUID.randomUUID());

        // fhir link
        FhirLink fhirLink = TestUtils.createFhirLink(patient, identifier);
        fhirLink.setGroup(staffEntered);

        when(codeService.findAllByCodeAndType(eq(code), eq(lookup))).thenReturn(codes);
        when(fhirLinkRepository.findByUserAndGroupAndIdentifier(eq(patient), eq(staffEntered), eq(identifier)))
                .thenReturn(null);
        when(fhirLinkService.createFhirLink(eq(patient), eq(patient.getIdentifiers().iterator().next()),
                eq(staffEntered))).thenReturn(fhirLink);
        when(fhirResource.createEntity(any(Patient.class), eq(ResourceType.Patient.name()), eq("patient")))
                .thenReturn(fhirPatient);
        when(fhirResource.createEntity(any(Practitioner.class), eq(ResourceType.Practitioner.name()),
                eq("practitioner"))).thenReturn(fhirPractitioner);
        when(groupService.findByCode(eq(HiddenGroupCodes.STAFF_ENTERED.toString()))).thenReturn(staffEntered);
        when(lookupService.findByTypeAndValue(eq(LookupTypes.CODE_TYPE), eq(CodeTypes.DIAGNOSIS.toString())))
                .thenReturn(lookup);
        when(patientService.buildPatient(eq(patient), eq(identifier))).thenReturn(new Patient());
        when(practitionerService.getPractitionerLogicalUuidsByName(eq(staffUser.getId().toString())))
                .thenReturn(null);
        when(userService.get(eq(patient.getId()))).thenReturn(patient);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);

        apiConditionService.staffAddCondition(patient.getId(), code);

        verify(fhirResource, times(1)).createEntity(
                any(Condition.class), eq(ResourceType.Condition.name()), eq("condition"));

        // required in adding FHIR link
        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(patient.getIdentifiers().iterator().next()),
                eq(staffEntered));

        // required when adding practitioner
        verify(fhirResource, times(1)).createEntity(
                any(Practitioner.class), eq(ResourceType.Practitioner.name()), eq("practitioner"));
    }

    @Test
    public void testStaffRemoveCondition() throws Exception {
        String code = "00";

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User staffUser = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, staffUser);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staffUser.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(staffUser, groupRoles);

        // patient
        User patient = TestUtils.createUser("patient");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        patient.setGroupRoles(groupRolesPatient);

        // patient identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), patient, "1111111111");

        // staff entered results group
        Group staffEntered = TestUtils.createGroup("staffEntered");
        staffEntered.setCode(HiddenGroupCodes.STAFF_ENTERED.toString());

        // fhir link
        FhirLink fhirLink = TestUtils.createFhirLink(patient, identifier);
        fhirLink.setGroup(staffEntered);
        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.add(fhirLink);

        // returned conditions
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.setTextSimple(code);
        Condition condition = new Condition();
        condition.setCode(codeableConcept);
        condition.setStatusSimple(Condition.ConditionStatus.confirmed);
        List<Condition> conditions = new ArrayList<>();
        conditions.add(condition);

        // UUID for returned condition
        UUID uuid = UUID.randomUUID();
        List<UUID> uuids = new ArrayList<>();
        uuids.add(uuid);

        when(fhirLinkRepository.findByUserAndGroupAndIdentifier(eq(patient), eq(staffEntered), eq(identifier)))
                .thenReturn(fhirLinks);
        when(groupService.findByCode(eq(HiddenGroupCodes.STAFF_ENTERED.toString()))).thenReturn(staffEntered);
        when(userService.get(eq(patient.getId()))).thenReturn(patient);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);
        when(fhirResource.findResourceByQuery(eq("SELECT content::varchar " + "FROM condition "
                + "WHERE content -> 'subject' ->> 'display' = '"
                + fhirLink.getResourceId() + "' "), eq(Condition.class))).thenReturn(conditions);

        when(fhirResource.getLogicalIdsBySubjectId(
                eq(ResourceType.Condition.getPath()), eq(fhirLinks.get(0).getResourceId()))).thenReturn(uuids);
        when(DataUtils.getResource(any(JSONObject.class))).thenReturn(condition);

        // get
        List<FhirCondition> conditionsList = apiConditionService.getStaffEntered(patient.getId());

        verify(fhirResource, times(1)).findResourceByQuery(eq("SELECT content::varchar FROM condition "
                + "WHERE content -> 'subject' ->> 'display' = '"
                + fhirLink.getResourceId() + "' "), eq(Condition.class));
        Assert.assertEquals("There should be 1 condition", 1, conditionsList.size());

        // remove
        apiConditionService.staffRemoveCondition(patient.getId());
        verify(fhirResource, times(1)).getLogicalIdsBySubjectId(
                eq(ResourceType.Condition.getPath()), eq(fhirLinks.get(0).getResourceId()));
        verify(fhirResource, times(1)).getResource(eq(uuid), eq(ResourceType.Condition));
        verify(fhirResource, times(1)).updateEntity(eq(condition), eq(ResourceType.Condition.getPath()),
                eq(ResourceType.Condition.getPath()), eq(uuid));
    }
}
