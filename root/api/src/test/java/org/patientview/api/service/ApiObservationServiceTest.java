package org.patientview.api.service;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceReference;
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
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.IdValue;
import org.patientview.api.model.UserResultCluster;
import org.patientview.api.service.impl.ApiObservationServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservationRange;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.ObservationHeadingGroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.ResultClusterRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.AuditService;
import org.patientview.service.FhirLinkService;
import org.patientview.service.ObservationService;
import org.patientview.service.PatientService;
import org.patientview.test.util.TestUtils;
import org.patientview.util.Util;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(Util.class)
public class ApiObservationServiceTest {

    User creator;

    @Mock
    AlertRepository alertRepository;

    @InjectMocks
    ApiObservationService apiObservationService = new ApiObservationServiceImpl();

    @Mock
    AuditService auditService;

    @Mock
    FhirLinkService fhirLinkService;

    @Mock
    FhirResource fhirResource;

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupService groupService;

    @Mock
    IdentifierRepository identifierRepository;

    @Mock
    ObservationHeadingGroupRepository observationHeadingGroupRepository;

    @Mock
    ObservationHeadingRepository observationHeadingRepository;

    @Mock
    ObservationService observationService;

    @Mock
    PatientService patientService;

    @Mock
    ResultClusterRepository resultClusterRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserService userService;

    private Date now;
    private Date weekAgo;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
        mockStatic(Util.class);
        this.now = new Date();
        this.weekAgo = new org.joda.time.DateTime(now).minusWeeks(1).toDate();
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testAddResultClusters() throws Exception {
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setId(2L);
        identifier.setIdentifier("1111111111");
        user.getIdentifiers().add(identifier);

        Group patientEnteredGroup = TestUtils.createGroup("testGroup");
        patientEnteredGroup.setCode(HiddenGroupCodes.PATIENT_ENTERED.toString());

        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        ObservationHeading observationHeading1 = new ObservationHeading();
        observationHeading1.setId(3L);
        observationHeading1.setCode("OBS1");

        List<ObservationHeading> observationHeadings = new ArrayList<>();
        observationHeadings.add(observationHeading1);

        List<UserResultCluster> userResultClusters = new ArrayList<>();

        IdValue value = new IdValue();
        value.setId(observationHeading1.getId());
        value.setValue("10.0");

        UserResultCluster userResultCluster = new UserResultCluster();
        userResultCluster.setComment("comment");
        userResultCluster.setDay("01");
        userResultCluster.setMonth("01");
        userResultCluster.setYear("2001");
        userResultCluster.setValues(new ArrayList<IdValue>());
        userResultCluster.getValues().add(value);
        userResultClusters.add(userResultCluster);

        Patient fhirPatient = new Patient();

        JSONObject fhirPatientJson = new JSONObject();
        String versionId = "31d2f326-230a-4ce0-879b-443154a4d9e6";
        String resourceId = "d52847eb-c2c7-4015-ba6c-952962536287";

        JSONArray resultArray = new JSONArray();
        JSONObject resource = new JSONObject();
        JSONArray links = new JSONArray();
        JSONObject link = new JSONObject();
        link.put("href", "http://www.patientview.org/patient/" + versionId);
        links.put(link);
        resource.put("link", links);
        resource.put("id", resourceId);
        resultArray.put(resource);
        fhirPatientJson.put("entry", resultArray);

        FhirDatabaseEntity entity = new FhirDatabaseEntity(fhirPatientJson.toString(), ResourceType.Patient.name());
        entity.setLogicalId(UUID.randomUUID());

        Observation observation = new Observation();

        when(fhirResource.createEntity(eq(fhirPatient), eq(ResourceType.Patient.name()), eq("patient")))
                .thenReturn(entity);
        when(fhirResource.marshallFhirRecord(any(Observation.class))).thenReturn("{}");
        when(groupRepository.findByCode(eq(HiddenGroupCodes.PATIENT_ENTERED.toString())))
                .thenReturn(patientEnteredGroup);
        when(observationHeadingRepository.findByCode(eq("resultcomment"))).thenReturn(observationHeadings);
        when(observationHeadingRepository.findOne(eq(observationHeading1.getId()))).thenReturn(observationHeading1);
        when(observationService.buildObservation(any(DateTime.class), any(String.class), any(String.class),
                any(String.class), eq(observationHeading1), any(Boolean.class))).thenReturn(observation);
        when(patientService.buildPatient(eq(user), eq(identifier))).thenReturn(fhirPatient);
        when(userRepository.findOne(Matchers.eq(user.getId()))).thenReturn(user);
        when(Util.createResourceReference(any(UUID.class))).thenReturn(new ResourceReference());

        apiObservationService.addUserResultClusters(user.getId(), userResultClusters);

        verify(observationService, times(1)).insertFhirDatabaseObservations(any(List.class));
    }

    @Test
    public void testAddObservations()
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

        Group group = TestUtils.createGroup("testGroup");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN_API);

        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        User patient = TestUtils.createUser("testUser");
        patient.getGroupRoles().add(TestUtils.createGroupRole(patientRole, group, patient));
        patient.setFhirLinks(new HashSet<FhirLink>());
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(patient);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287"));
        patient.getFhirLinks().add(fhirLink);

        FhirObservationRange fhirObservationRange = new FhirObservationRange();
        fhirObservationRange.setCode("wbc");
        fhirObservationRange.setStartDate(new Date());
        fhirObservationRange.setEndDate(new Date());
        fhirObservationRange.setObservations(new ArrayList<org.patientview.persistence.model.FhirObservation>());

        org.patientview.persistence.model.FhirObservation fhirObservation
                = new org.patientview.persistence.model.FhirObservation();
        fhirObservation.setApplies(new Date());
        fhirObservation.setValue("999");
        fhirObservationRange.getObservations().add(fhirObservation);

        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(fhirResource.marshallFhirRecord(any(Observation.class)))
                .thenReturn("{\"applies\": \"2013-10-31T00:00:00\",\"value\": \"999\"}");

        apiObservationService.addTestObservations(patient.getId(), group.getId(), fhirObservationRange);
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testAddObservations_incorrectGroup()
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

        Group group = TestUtils.createGroup("testGroup");
        Group group2 = TestUtils.createGroup("testGroup2");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN_API);

        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group2, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        User patient = TestUtils.createUser("testUser");
        patient.getGroupRoles().add(TestUtils.createGroupRole(patientRole, group, patient));
        patient.setFhirLinks(new HashSet<FhirLink>());
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(patient);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287"));
        patient.getFhirLinks().add(fhirLink);

        FhirObservationRange fhirObservationRange = new FhirObservationRange();
        fhirObservationRange.setCode("wbc");
        fhirObservationRange.setStartDate(new Date());
        fhirObservationRange.setEndDate(new Date());
        fhirObservationRange.setObservations(new ArrayList<org.patientview.persistence.model.FhirObservation>());

        org.patientview.persistence.model.FhirObservation fhirObservation
                = new org.patientview.persistence.model.FhirObservation();
        fhirObservation.setApplies(new Date());
        fhirObservation.setValue("999");
        fhirObservationRange.getObservations().add(fhirObservation);

        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(fhirResource.marshallFhirRecord(any(Observation.class)))
                .thenReturn("{\"applies\": \"2013-10-31T00:00:00\",\"value\": \"999\"}");

        apiObservationService.addTestObservations(patient.getId(), group.getId(), fhirObservationRange);
    }


    @Test
    public void testAddDialysisTreatmentResult() throws Exception {
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setId(2L);
        identifier.setIdentifier("1111111111");
        user.getIdentifiers().add(identifier);

        Group patientEnteredGroup = TestUtils.createGroup("testGroup");
        patientEnteredGroup.setCode(HiddenGroupCodes.PATIENT_ENTERED.toString());

        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        ObservationHeading observationHeading1 = new ObservationHeading();
        observationHeading1.setId(3L);
        observationHeading1.setCode("eprex");

        List<ObservationHeading> observationHeadings = new ArrayList<>();
        observationHeadings.add(observationHeading1);

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("day","01");
        resultMap.put("month","01");
        resultMap.put("year","2017");
        resultMap.put("HdHours","1");
        resultMap.put("HdLocation","Amex");
        resultMap.put("eprex","12");
        resultMap.put("TargetWeight","90");
        resultMap.put("PreWeight","80");
        resultMap.put("PostWeight","90");
        resultMap.put("UfVolume","12");
        resultMap.put("pulse","123");
        resultMap.put("PreBpsys","12");
        resultMap.put("PreBpdia","122");
        resultMap.put("PostBpsys","123");
        resultMap.put("PostBpdia","123");

        Patient fhirPatient = new Patient();

        JSONObject fhirPatientJson = new JSONObject();
        String versionId = "31d2f326-230a-4ce0-879b-443154a4d9e6";
        String resourceId = "d52847eb-c2c7-4015-ba6c-952962536287";

        JSONArray resultArray = new JSONArray();
        JSONObject resource = new JSONObject();
        JSONArray links = new JSONArray();
        JSONObject link = new JSONObject();
        link.put("href", "http://www.patientview.org/patient/" + versionId);
        links.put(link);
        resource.put("link", links);
        resource.put("id", resourceId);
        resultArray.put(resource);
        fhirPatientJson.put("entry", resultArray);

        FhirDatabaseEntity entity = new FhirDatabaseEntity(fhirPatientJson.toString(), ResourceType.Patient.name());
        entity.setLogicalId(UUID.randomUUID());

        Observation observation = new Observation();

        when(fhirResource.createEntity(eq(fhirPatient), eq(ResourceType.Patient.name()), eq("patient")))
                .thenReturn(entity);
        when(fhirResource.marshallFhirRecord(any(Observation.class))).thenReturn("{}");
        when(groupRepository.findByCode(eq(HiddenGroupCodes.PATIENT_ENTERED.toString())))
                .thenReturn(patientEnteredGroup);
        when(observationHeadingRepository.findByCode(eq("resultcomment"))).thenReturn(observationHeadings);
        when(observationHeadingRepository.findOneByCode(any(String.class))).thenReturn(observationHeading1);
        when(observationService.buildObservation(any(DateTime.class), any(String.class), any(String.class),
                any(String.class), eq(observationHeading1), any(Boolean.class))).thenReturn(observation);
        when(patientService.buildPatient(eq(user), eq(identifier))).thenReturn(fhirPatient);
        when(userRepository.findOne(Matchers.eq(user.getId()))).thenReturn(user);
        when(Util.createResourceReference(any(UUID.class))).thenReturn(new ResourceReference());

        apiObservationService.addUserDialysisTreatmentResult(user.getId(), resultMap);

        verify(observationService, times(1)).insertFhirDatabaseObservations(any(List.class));
    }


    @Test
    public void testUpdatePatientEnteredResult()
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        UUID uuid = UUID.randomUUID();
        Group group = TestUtils.createGroup("PATIENT_ENTERED");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);

        User patient = TestUtils.createUser("testUser");
        patient.getGroupRoles().add(TestUtils.createGroupRole(patientRole, group, patient));
        patient.setFhirLinks(new HashSet<FhirLink>());
        TestUtils.authenticateTest(patient, patient.getGroupRoles());

        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(patient);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(UUID.randomUUID());
        patient.getFhirLinks().add(fhirLink);

        patient.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setId(2L);
        identifier.setIdentifier("1111111111");
        patient.getIdentifiers().add(identifier);

        FhirObservation fhirObservation = new FhirObservation();
        fhirObservation.setLogicalId(uuid);
        fhirObservation.setApplies(new Date());
        fhirObservation.setValue("999");

        List<UUID> foundIds = new ArrayList<>();
        foundIds.add(uuid);

        // Observation
        String code = "weight";
        String value = "100";
        Observation observation = new Observation();
        CodeableConcept valueConcept = new CodeableConcept();
        valueConcept.setTextSimple(value);
        valueConcept.addCoding().setDisplaySimple(value);
        observation.setValue(valueConcept);

        DateTime dateTime = new DateTime();
        DateAndTime dateAndTime = new DateAndTime(new Date());
        dateTime.setValue(dateAndTime);
        observation.setApplies(dateTime);

        CodeableConcept nameConcept = new CodeableConcept();
        nameConcept.setTextSimple(code);
        nameConcept.addCoding().setDisplaySimple(code);
        observation.setName(nameConcept);

        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(groupRepository.findByCode(any(String.class))).thenReturn(group);
        when(fhirResource.getLogicalIdsBySubjectId(any(String.class), eq(fhirLink.getResourceId())))
                .thenReturn(foundIds);
        when(fhirResource.get(eq(uuid), eq(ResourceType.Observation))).thenReturn(observation);
        when(observationService.copyObservation(eq(observation), eq(fhirObservation.getApplies()),
                eq(fhirObservation.getValue()))).thenReturn(observation);

        apiObservationService.updatePatientEnteredResult(patient.getId(), null, fhirObservation);

        // verify
        verify(fhirResource, times(1)).updateEntity(eq(observation), eq("observation"),
                eq("observation"),eq(fhirObservation.getLogicalId()));
        verify(auditService, times(1)).save(any(Audit.class));
    }

    @Test
    public void testDeletePatientEnteredResult()
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        UUID uuid = UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287");

        Group group = TestUtils.createGroup("PATIENT_ENTERED");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");
        patient.getGroupRoles().add(TestUtils.createGroupRole(patientRole, group, patient));
        patient.setFhirLinks(new HashSet<FhirLink>());
        TestUtils.authenticateTest(patient, patient.getGroupRoles());

        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(patient);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(uuid);
        patient.getFhirLinks().add(fhirLink);

        patient.setIdentifiers(new HashSet<Identifier>());
        Identifier identifier = new Identifier();
        identifier.setId(2L);
        identifier.setIdentifier("1111111111");
        patient.getIdentifiers().add(identifier);

        List<UUID> foundIds = new ArrayList<>();
        foundIds.add(uuid);

        // Observation
        String code = "weight";
        String value = "100";
        Observation observation = new Observation();
        CodeableConcept valueConcept = new CodeableConcept();
        valueConcept.setTextSimple(value);
        valueConcept.addCoding().setDisplaySimple(value);
        observation.setValue(valueConcept);

        DateTime dateTime = new DateTime();
        DateAndTime dateAndTime = new DateAndTime(new Date());
        dateTime.setValue(dateAndTime);
        observation.setApplies(dateTime);

        CodeableConcept nameConcept = new CodeableConcept();
        nameConcept.setTextSimple(code);
        nameConcept.addCoding().setDisplaySimple(code);
        observation.setName(nameConcept);

        // when
        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(groupRepository.findByCode(any(String.class))).thenReturn(group);
        when(fhirResource.getLogicalIdsBySubjectId(any(String.class), eq(fhirLink.getResourceId())))
                .thenReturn(foundIds);
        when(fhirResource.get(eq(fhirLink.getResourceId()), eq(ResourceType.Observation))).thenReturn(observation);

        apiObservationService.deletePatientEnteredResult(patient.getId(), null, uuid.toString());

        // verify
        verify(fhirResource, times(1)).deleteEntity(eq(uuid), eq("observation"));
        verify(auditService, times(1)).save(any(Audit.class));
    }

    @Test
    public void testGetObservations()
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

        Group group = TestUtils.createGroup("testGroup");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN_API);

        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        User patient = TestUtils.createUser("testUser");
        patient.getGroupRoles().add(TestUtils.createGroupRole(patientRole, group, patient));
        patient.setFhirLinks(new HashSet<FhirLink>());
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(patient);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287"));
        fhirLink.setActive(true);
        patient.getFhirLinks().add(fhirLink);

        String code = "wbc";
        String value = "999";

        List<Observation> fhirObservations = new ArrayList<>();

        Observation observation = new Observation();
        CodeableConcept valueConcept = new CodeableConcept();
        valueConcept.setTextSimple(value);
        valueConcept.addCoding().setDisplaySimple(value);
        observation.setValue(valueConcept);

        DateTime dateTime = new DateTime();
        DateAndTime dateAndTime = new DateAndTime(new Date());
        dateTime.setValue(dateAndTime);
        observation.setApplies(dateTime);
        fhirObservations.add(observation);

        CodeableConcept nameConcept = new CodeableConcept();
        nameConcept.setTextSimple(code);
        nameConcept.addCoding().setDisplaySimple(code);
        observation.setName(nameConcept);


        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(fhirResource.findResourceByQuery(any(String.class), eq(Observation.class)))
                .thenReturn(fhirObservations);

        List<FhirObservation> apiObservations
                = apiObservationService.get(patient.getId(), code, "appliesDateTime", "ASC", Long.MAX_VALUE);

        Assert.assertEquals("Should return observations", true, apiObservations.size() > 0);
        Assert.assertEquals("Should return 1 observation", 1, apiObservations.size());
        Assert.assertEquals("Should return correct observation", value, apiObservations.get(0).getValue());
    }

    @Test
    public void testGetObservations_ownObservations()
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

        Group group = TestUtils.createGroup("testGroup");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        User patient = TestUtils.createUser("testUser");

        GroupRole groupRole = TestUtils.createGroupRole(patientRole, group, patient);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        patient.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(patient, groupRoles);

        patient.getGroupRoles().add(TestUtils.createGroupRole(patientRole, group, patient));
        patient.setFhirLinks(new HashSet<FhirLink>());
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(patient);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287"));
        fhirLink.setActive(true);
        patient.getFhirLinks().add(fhirLink);

        String code = "wbc";
        String value = "999";

        List<Observation> fhirObservations = new ArrayList<>();

        Observation observation = new Observation();
        CodeableConcept valueConcept = new CodeableConcept();
        valueConcept.setTextSimple(value);
        valueConcept.addCoding().setDisplaySimple(value);
        observation.setValue(valueConcept);

        DateTime dateTime = new DateTime();
        DateAndTime dateAndTime = new DateAndTime(new Date());
        dateTime.setValue(dateAndTime);
        observation.setApplies(dateTime);
        fhirObservations.add(observation);

        CodeableConcept nameConcept = new CodeableConcept();
        nameConcept.setTextSimple(code);
        nameConcept.addCoding().setDisplaySimple(code);
        observation.setName(nameConcept);


        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(fhirResource.findResourceByQuery(any(String.class), eq(Observation.class)))
                .thenReturn(fhirObservations);

        List<FhirObservation> apiObservations
                = apiObservationService.get(patient.getId(), code, "appliesDateTime", "ASC", Long.MAX_VALUE);

        Assert.assertEquals("Should return observations", true, apiObservations.size() > 0);
        Assert.assertEquals("Should return 1 observation", 1, apiObservations.size());
        Assert.assertEquals("Should return correct observation", value, apiObservations.get(0).getValue());
    }

    @Test(expected = ResourceForbiddenException.class)
    public void testGetObservations_incorrectRole()
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

        Group group = TestUtils.createGroup("testGroup");
        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN);

        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        User patient = TestUtils.createUser("testUser");
        patient.getGroupRoles().add(TestUtils.createGroupRole(patientRole, group, patient));
        patient.setFhirLinks(new HashSet<FhirLink>());
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(patient);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287"));
        fhirLink.setActive(true);
        patient.getFhirLinks().add(fhirLink);

        String code = "wbc";
        String value = "999";

        List<Observation> fhirObservations = new ArrayList<>();

        Observation observation = new Observation();
        CodeableConcept valueConcept = new CodeableConcept();
        valueConcept.setTextSimple(value);
        valueConcept.addCoding().setDisplaySimple(value);
        observation.setValue(valueConcept);

        DateTime dateTime = new DateTime();
        DateAndTime dateAndTime = new DateAndTime(new Date());
        dateTime.setValue(dateAndTime);
        observation.setApplies(dateTime);
        fhirObservations.add(observation);

        CodeableConcept nameConcept = new CodeableConcept();
        nameConcept.setTextSimple(code);
        nameConcept.addCoding().setDisplaySimple(code);
        observation.setName(nameConcept);


        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(fhirResource.findResourceByQuery(any(String.class), eq(Observation.class)))
                .thenReturn(fhirObservations);

        List<FhirObservation> apiObservations
                = apiObservationService.get(patient.getId(), code, "appliesDateTime", "ASC", Long.MAX_VALUE);

        Assert.assertEquals("Should return observations", true, apiObservations.size() > 0);
        Assert.assertEquals("Should return 1 observation", 1, apiObservations.size());
        Assert.assertEquals("Should return correct observation", value, apiObservations.get(0).getValue());
    }

    @Test
    public void testImportObservations() throws Exception {
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

        // FhirObservationRange
        FhirObservationRange fhirObservationRange = new FhirObservationRange();
        fhirObservationRange.setGroupCode("DSF01");
        fhirObservationRange.setIdentifier("1111111111");
        fhirObservationRange.setStartDate(new Date());
        fhirObservationRange.setEndDate(new Date());
        fhirObservationRange.setCode("hb");

        // observations
        org.patientview.persistence.model.FhirObservation fhirObservation
                = new org.patientview.persistence.model.FhirObservation();
        fhirObservation.setApplies(new Date());
        fhirObservation.setValue("20.16");
        fhirObservationRange.getObservations().add(fhirObservation);

        // observation heading
        ObservationHeading observationHeading = new ObservationHeading();
        observationHeading.setCode(fhirObservationRange.getCode());
        List<ObservationHeading> observationHeadings = new ArrayList<>();
        observationHeadings.add(observationHeading);

        // existing observation uuids
        List<UUID> existingObservations = new ArrayList<>();
        existingObservations.add(UUID.randomUUID());

        // Alert set up by patient for new letters
        Alert alert = new Alert();
        alert.setAlertType(AlertTypes.RESULT);
        alert.setObservationHeading(observationHeading);
        alert.setLatestDate(weekAgo);
        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);

        when(alertRepository.findByUserAndObservationHeading(eq(patient), eq(observationHeading)))
                .thenReturn(alerts);
        when(alertRepository.findOne(eq(alert.getId()))).thenReturn(alert);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(fhirResource.getObservationUuidsBySubjectNameDateRange(any(UUID.class),
                eq(fhirObservationRange.getCode()), eq(fhirObservationRange.getStartDate()),
                eq(fhirObservationRange.getStartDate()))).thenReturn(existingObservations);
        when(fhirResource.marshallFhirRecord(any(Resource.class)))
                .thenReturn("{\"applies\": \"2013-10-31T00:00:00\",\"value\": \"999\"}");
        when(groupRepository.findByCode(eq(fhirObservationRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirObservationRange.getIdentifier()))).thenReturn(identifiers);
        when(observationHeadingRepository.findByCode(eq(fhirObservationRange.getCode())))
                .thenReturn(observationHeadings);

        ServerResponse serverResponse = apiObservationService.importObservations(fhirObservationRange);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct added success message, got '"
                + serverResponse.getSuccessMessage() + "'", serverResponse.getSuccessMessage().contains("added 1"));
        Assert.assertTrue("Should have correct deleted success message, got '"
                + serverResponse.getSuccessMessage() + "'", serverResponse.getSuccessMessage().contains("deleted 1"));

        verify(alertRepository, times(1)).findByUserAndObservationHeading(eq(patient), eq(observationHeading));
        verify(alertRepository, times(1)).save(any(Alert.class));
        verify(auditService, times(1)).createAudit(eq(AuditActions.PATIENT_DATA_SUCCESS), eq(patient.getUsername()),
                any(User.class), eq(patient.getId()), eq(AuditObjectTypes.User), any(Group.class));
        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(fhirResource, times(1)).marshallFhirRecord(any(Observation.class));
        verify(observationService, times(1)).deleteObservations(any(List.class));
        verify(observationService, times(1)).insertFhirDatabaseObservations(any(List.class));
    }

    @Test
    public void testImportObservations_addOnly() throws Exception {
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

        // FhirObservationRange
        FhirObservationRange fhirObservationRange = new FhirObservationRange();
        fhirObservationRange.setGroupCode("DSF01");
        fhirObservationRange.setIdentifier("1111111111");
        fhirObservationRange.setCode("hb");

        // observations
        org.patientview.persistence.model.FhirObservation fhirObservation
                = new org.patientview.persistence.model.FhirObservation();
        fhirObservation.setApplies(new Date());
        fhirObservation.setValue("20.16");
        fhirObservationRange.getObservations().add(fhirObservation);

        // observation heading
        ObservationHeading observationHeading = new ObservationHeading();
        observationHeading.setCode(fhirObservationRange.getCode());
        List<ObservationHeading> observationHeadings = new ArrayList<>();
        observationHeadings.add(observationHeading);

        // Alert set up by patient for new letters
        Alert alert = new Alert();
        alert.setAlertType(AlertTypes.RESULT);
        alert.setObservationHeading(observationHeading);
        alert.setLatestDate(weekAgo);
        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);

        when(alertRepository.findByUserAndObservationHeading(eq(patient), eq(observationHeading)))
                .thenReturn(alerts);
        when(alertRepository.findOne(eq(alert.getId()))).thenReturn(alert);
        when(fhirLinkService.createFhirLink(eq(patient), eq(identifier), eq(group)))
                .thenReturn(patient.getFhirLinks().iterator().next());
        when(fhirResource.marshallFhirRecord(any(Resource.class)))
                .thenReturn("{\"applies\": \"2013-10-31T00:00:00\",\"value\": \"999\"}");
        when(groupRepository.findByCode(eq(fhirObservationRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirObservationRange.getIdentifier()))).thenReturn(identifiers);
        when(observationHeadingRepository.findByCode(eq(fhirObservationRange.getCode())))
                .thenReturn(observationHeadings);

        ServerResponse serverResponse = apiObservationService.importObservations(fhirObservationRange);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct success message, got '" + serverResponse.getSuccessMessage() + "'",
                serverResponse.getSuccessMessage().contains("added 1"));

        verify(alertRepository, times(1)).findByUserAndObservationHeading(eq(patient), eq(observationHeading));
        verify(alertRepository, times(1)).save(any(Alert.class));
        verify(auditService, times(1)).createAudit(eq(AuditActions.PATIENT_DATA_SUCCESS), eq(patient.getUsername()),
                any(User.class), eq(patient.getId()), eq(AuditObjectTypes.User), any(Group.class));
        verify(fhirLinkService, times(1)).createFhirLink(eq(patient), eq(identifier), eq(group));
        verify(fhirResource, times(1)).marshallFhirRecord(any(Observation.class));
        verify(observationService, times(0)).deleteObservations(any(List.class));
        verify(observationService, times(1)).insertFhirDatabaseObservations(any(List.class));
    }

    @Test
    public void testImportObservations_onlyDelete() throws Exception {
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

        // fhirlinks, in this case patient does not need a new fhirlink creating
        TestUtils.createFhirLink(patient, identifier, group);

        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // FhirObservationRange
        FhirObservationRange fhirObservationRange = new FhirObservationRange();
        fhirObservationRange.setGroupCode("DSF01");
        fhirObservationRange.setIdentifier("1111111111");
        fhirObservationRange.setStartDate(new Date());
        fhirObservationRange.setEndDate(new Date());
        fhirObservationRange.setCode("hb");

        // observation heading
        ObservationHeading observationHeading = new ObservationHeading();
        observationHeading.setCode(fhirObservationRange.getCode());
        List<ObservationHeading> observationHeadings = new ArrayList<>();
        observationHeadings.add(observationHeading);

        // built fhir patient
        Patient builtPatient = new Patient();

        // created fhir patient
        FhirDatabaseEntity fhirPatient = new FhirDatabaseEntity();
        fhirPatient.setLogicalId(UUID.randomUUID());

        // existing observation uuids
        List<UUID> existingObservations = new ArrayList<>();
        existingObservations.add(UUID.randomUUID());

        when(fhirResource.createEntity(eq(builtPatient), eq(ResourceType.Patient.name()),
                eq("patient"))).thenReturn(fhirPatient);
        when(fhirResource.getObservationUuidsBySubjectNameDateRange(any(UUID.class),
                eq(fhirObservationRange.getCode()), eq(fhirObservationRange.getStartDate()),
                eq(fhirObservationRange.getStartDate()))).thenReturn(existingObservations);
        when(fhirResource.marshallFhirRecord(any(Resource.class)))
                .thenReturn("{\"applies\": \"2013-10-31T00:00:00\",\"value\": \"999\"}");
        when(groupRepository.findByCode(eq(fhirObservationRange.getGroupCode()))).thenReturn(group);
        when(identifierRepository.findByValue(eq(fhirObservationRange.getIdentifier()))).thenReturn(identifiers);
        when(observationHeadingRepository.findByCode(eq(fhirObservationRange.getCode())))
                .thenReturn(observationHeadings);
        when(patientService.buildPatient(eq(patient), eq(identifier))).thenReturn(builtPatient);
        when(Util.getFhirLink(eq(group), eq(identifier.getIdentifier()), eq(patient.getFhirLinks())))
                .thenReturn(patient.getFhirLinks().iterator().next());

        ServerResponse serverResponse = apiObservationService.importObservations(fhirObservationRange);

        Assert.assertTrue(
                "Should be successful, got '" + serverResponse.getErrorMessage() + "'", serverResponse.isSuccess());
        Assert.assertTrue("Should have correct success message, got '" + serverResponse.getSuccessMessage() + "'",
                serverResponse.getSuccessMessage().contains("deleted 1"));

        verify(alertRepository, times(0)).findByUserAndObservationHeading(eq(patient), eq(observationHeading));
        verify(alertRepository, times(0)).save(any(Alert.class));
        verify(auditService, times(0)).createAudit(eq(AuditActions.PATIENT_DATA_SUCCESS), eq(patient.getUsername()),
                any(User.class), eq(patient.getId()), eq(AuditObjectTypes.User), any(Group.class));
        verify(fhirResource, times(0)).createEntity(eq(builtPatient), eq(ResourceType.Patient.name()),
                eq("patient"));
        verify(fhirResource, times(0)).marshallFhirRecord(any(Observation.class));
        verify(observationService, times(1)).deleteObservations(any(List.class));
        verify(observationService, times(0)).insertFhirDatabaseObservations(any(List.class));
        verify(userRepository, times(0)).save(eq(patient));
    }

    @Test
    public void testGetPatientEnteredObservations()
            throws ResourceNotFoundException, ResourceForbiddenException,
            FhirResourceException, ResourceInvalidException {

        String testNhsNumber = "324234234";

        Group group = TestUtils.createGroup("PATIENT_ENTERED");
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.GROUP);
        Lookup type = TestUtils.createLookup(lookupType, GroupTypes.UNIT.toString());
        group.setGroupType(type);


        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        Role staffRole = TestUtils.createRole(RoleName.IMPORTER);

        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        User patient = TestUtils.createUser("testUser");
        patient.getGroupRoles().add(TestUtils.createGroupRole(patientRole, group, patient));
        TestUtils.createIdentifier(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString()), patient, testNhsNumber);
        patient.setFhirLinks(new HashSet<FhirLink>());
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(patient);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287"));
        fhirLink.setActive(true);
        patient.getFhirLinks().add(fhirLink);

        List<User> patients = new ArrayList<>();
        patients.add(patient);

        ObservationHeading observationHeading1 = new ObservationHeading();
        observationHeading1.setId(3L);
        observationHeading1.setCode("BPDIA");

        List<ObservationHeading> observationHeadings = new ArrayList<>();
        observationHeadings.add(observationHeading1);


        List<String[]> fhirObservationValues = new ArrayList<>();

        String[] value1 = new String[]{"2016-09-27T10:10:00.460+01:00", "bpdia", "33.07", null, null};
        String[] value2 = new String[]{"2016-09-26T17:17:00.313+01:00", "bpdia", "130.45", null, null};
        String[] value3 = new String[]{"2016-09-27T10:10:00.460+01:00", "bpdia", "25.05", null, null};
        String[] value4 = new String[]{"2016-09-26T17:17:00.313+01:00", "bpdia", "3100.05", null, null};
        fhirObservationValues.add(value1);
        fhirObservationValues.add(value2);
        fhirObservationValues.add(value3);
        fhirObservationValues.add(value4);


        //when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(userRepository.findByIdentifier(any(String.class))).thenReturn(patients);
        when(userService.currentUserSameUnitGroup(patient, RoleName.IMPORTER)).thenReturn(true);
        when(observationHeadingRepository.findAll()).thenReturn(observationHeadings);
        when(observationHeadingRepository.findAll()).thenReturn(observationHeadings);
        when(fhirResource.findLatestObservationsByQuery(any(String.class)))
                .thenReturn(fhirObservationValues);
        when(Util.convertIterable(observationHeadings)).thenReturn(observationHeadings);

        List<org.patientview.api.model.ObservationHeading> apiObservations
                = apiObservationService.getPatientEnteredObservations(testNhsNumber, null, null);

        Assert.assertEquals("Should return observations", true, apiObservations.size() > 0);
        Assert.assertEquals("Should return 1 observation", 1, apiObservations.size());
        Assert.assertTrue("Should have observation returned", apiObservations.get(0).getObservations().size() > 0);
        Assert.assertEquals("Should return 1 observation", 4, apiObservations.get(0).getObservations().size());
    }


    @Test(expected = ResourceInvalidException.class)
    public void testGetPatientEnteredObservations_MultiplePatients()
            throws ResourceNotFoundException, ResourceForbiddenException,
            FhirResourceException, ResourceInvalidException {

        String testNhsNumber = "324234234";

        Group group = TestUtils.createGroup("PATIENT_ENTERED");
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.GROUP);
        Lookup type = TestUtils.createLookup(lookupType, GroupTypes.UNIT.toString());
        group.setGroupType(type);

        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        Role staffRole = TestUtils.createRole(RoleName.IMPORTER);

        User staff = TestUtils.createUser("testStaff");
        GroupRole groupRole = TestUtils.createGroupRole(staffRole, group, staff);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        staff.getGroupRoles().add(groupRole);
        TestUtils.authenticateTest(staff, groupRoles);

        User patient1 = TestUtils.createUser("testUser");
        patient1.getGroupRoles().add(TestUtils.createGroupRole(patientRole, group, patient1));
        TestUtils.createIdentifier(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString()), patient1, testNhsNumber);
        patient1.setFhirLinks(new HashSet<FhirLink>());
        FhirLink fhirLink = new FhirLink();
        fhirLink.setUser(patient1);
        fhirLink.setGroup(group);
        fhirLink.setResourceId(UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287"));
        fhirLink.setActive(true);
        patient1.getFhirLinks().add(fhirLink);

        User patient2 = TestUtils.createUser("testUser");
        patient1.getGroupRoles().add(TestUtils.createGroupRole(patientRole, group, patient1));
        TestUtils.createIdentifier(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString()), patient2, testNhsNumber);
        patient2.setFhirLinks(new HashSet<FhirLink>());
        FhirLink fhirLink2 = new FhirLink();
        fhirLink2.setUser(patient2);
        fhirLink2.setGroup(group);
        fhirLink2.setResourceId(UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287"));
        fhirLink2.setActive(true);
        patient2.getFhirLinks().add(fhirLink);

        List<User> patients = new ArrayList<>();
        patients.add(patient1);
        patients.add(patient2);

        ObservationHeading observationHeading1 = new ObservationHeading();
        observationHeading1.setId(3L);
        observationHeading1.setCode("BPDIA");

        List<ObservationHeading> observationHeadings = new ArrayList<>();
        observationHeadings.add(observationHeading1);


        List<String[]> fhirObservationValues = new ArrayList<>();

        String[] value1 = new String[]{"2016-09-27T10:10:00.460+01:00", "bpdia", "33.07", null, null};
        String[] value2 = new String[]{"2016-09-26T17:17:00.313+01:00", "bpdia", "130.45", null, null};
        String[] value3 = new String[]{"2016-09-27T10:10:00.460+01:00", "bpdia", "25.05", null, null};
        String[] value4 = new String[]{"2016-09-26T17:17:00.313+01:00", "bpdia", "3100.05", null, null};
        fhirObservationValues.add(value1);
        fhirObservationValues.add(value2);
        fhirObservationValues.add(value3);
        fhirObservationValues.add(value4);

        //when(userRcurrentUserSameUnitGroupepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(userRepository.findByIdentifier(any(String.class))).thenReturn(patients);
        when(userService.currentUserSameUnitGroup(patient1, RoleName.IMPORTER)).thenReturn(true);
        when(observationHeadingRepository.findAll()).thenReturn(observationHeadings);
        when(observationHeadingRepository.findAll()).thenReturn(observationHeadings);
        when(fhirResource.findLatestObservationsByQuery(any(String.class)))
                .thenReturn(fhirObservationValues);
        when(Util.convertIterable(observationHeadings)).thenReturn(observationHeadings);

        List<org.patientview.api.model.ObservationHeading> apiObservations
                = apiObservationService.getPatientEnteredObservations(testNhsNumber, null, null);

        Assert.assertEquals("Should return observations", true, apiObservations.size() > 0);

    }
}