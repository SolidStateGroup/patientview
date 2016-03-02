package org.patientview.api.service;

import org.hl7.fhir.instance.model.CodeableConcept;
import org.hl7.fhir.instance.model.DateAndTime;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.FhirObservationRange;
import org.patientview.api.model.IdValue;
import org.patientview.api.model.UserResultCluster;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.api.service.impl.ApiObservationServiceImpl;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.ObservationHeadingGroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.persistence.repository.ResultClusterRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
public class ApiObservationServiceTest {

    User creator;

    @InjectMocks
    ApiObservationService apiObservationService = new ApiObservationServiceImpl();

    @Mock
    ApiPatientService apiPatientService;

    @Mock
    FhirResource fhirResource;

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupService groupService;

    @Mock
    ObservationHeadingGroupRepository observationHeadingGroupRepository;

    @Mock
    ObservationHeadingRepository observationHeadingRepository;

    @Mock
    ObservationHeadingService observationHeadingService;

    @Mock
    ResultClusterRepository resultClusterRepository;

    @Mock
    UserRepository userRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testAddResultClusters() {

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

        try {
            when(userRepository.findOne(Matchers.eq(user.getId()))).thenReturn(user);
            when(observationHeadingService.get(eq(observationHeading1.getId()))).thenReturn(observationHeading1);
            when(observationHeadingService.findByCode(eq("resultcomment"))).thenReturn(observationHeadings);
            when(groupService.findByCode(eq(HiddenGroupCodes.PATIENT_ENTERED.toString()))).thenReturn(patientEnteredGroup);
            when(apiPatientService.buildPatient(eq(user), eq(identifier))).thenReturn(fhirPatient);
            when(fhirResource.createEntity(eq(fhirPatient), eq(ResourceType.Patient.name()), eq("patient"))).thenReturn(entity);

            when(fhirResource.marshallFhirRecord(any(Observation.class))).thenReturn("{}");

            apiObservationService.addUserResultClusters(user.getId(), userResultClusters);

        } catch (ResourceNotFoundException rnf) {
            Assert.fail("ResourceNotFoundException: " + rnf.getMessage());
        } catch (FhirResourceException fre) {
            Assert.fail("FhirResourceException: " + fre.getMessage());
        }
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
        fhirObservationRange.setObservations(new ArrayList<FhirObservation>());
        
        FhirObservation fhirObservation = new FhirObservation();
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
        fhirObservationRange.setObservations(new ArrayList<FhirObservation>());

        FhirObservation fhirObservation = new FhirObservation();
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
}
