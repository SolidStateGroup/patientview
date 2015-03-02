package org.patientview.api.service;

import org.hl7.fhir.instance.model.Observation;
import org.hl7.fhir.instance.model.Patient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.FhirObservationRange;
import org.patientview.api.model.IdValue;
import org.patientview.api.model.UserResultCluster;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.HiddenGroupCodes;
import org.patientview.api.service.impl.ObservationServiceImpl;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
public class ObservationServiceTest {

    User creator;

    @Mock
    FhirResource fhirResource;

    @Mock
    GroupService groupService;

    @Mock
    GroupRepository groupRepository;

    @Mock
    ObservationHeadingGroupRepository observationHeadingGroupRepository;

    @Mock
    ObservationHeadingRepository observationHeadingRepository;

    @Mock
    ObservationHeadingService observationHeadingService;

    @InjectMocks
    ObservationService observationService = new ObservationServiceImpl();

    @Mock
    PatientService patientService;

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

        try {
            when(userRepository.findOne(Matchers.eq(user.getId()))).thenReturn(user);
            when(observationHeadingService.get(eq(observationHeading1.getId()))).thenReturn(observationHeading1);
            when(observationHeadingService.findByCode(eq("resultcomment"))).thenReturn(observationHeadings);
            when(groupService.findByCode(eq(HiddenGroupCodes.PATIENT_ENTERED.toString()))).thenReturn(patientEnteredGroup);
            when(patientService.buildPatient(eq(user), eq(identifier))).thenReturn(fhirPatient);
            when(fhirResource.create(eq(fhirPatient))).thenReturn(fhirPatientJson);

            when(fhirResource.marshallFhirRecord(any(Observation.class))).thenReturn("{}");

            observationService.addUserResultClusters(user.getId(), userResultClusters);

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
        patient.getFhirLinks().add(fhirLink);

        FhirObservationRange fhirObservationRange = new FhirObservationRange();
        fhirObservationRange.setCode("wbc");
        fhirObservationRange.setStartDate(new Date());
        fhirObservationRange.setEndDate(new Date());
        fhirObservationRange.setObservations(new ArrayList<FhirObservation>());
        
        FhirObservation fhirObservation = new FhirObservation();
        fhirObservation.setApplies(new Date());
        fhirObservation.setValue("99");
        fhirObservationRange.getObservations().add(fhirObservation);

        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);
        when(fhirResource.marshallFhirRecord(any(Observation.class)))
                .thenReturn("{\"applies\": \"2013-10-31T00:00:00\",\"value\": \"999\"}");

        observationService.addTestObservations(patient.getId(), group.getId(), fhirObservationRange);
    }
}
