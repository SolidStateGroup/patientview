package org.patientview.api.service;

import org.hl7.fhir.instance.model.Patient;
import org.hl7.fhir.instance.model.ResourceType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.PatientServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.test.util.TestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;

/**
 * Tests for PatientService, used for reading and writing patient record in FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 03/03/2015
 */
public class PatientServiceTest {
    
    @InjectMocks
    PatientService patientService = new PatientServiceImpl();

    private User creator;

    @Mock
    private FhirLinkRepository fhirLinkRepository;

    @Mock
    private FhirResource fhirResource;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

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
    public void testUpdate() throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {

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

        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        when(groupRepository.findOne(Matchers.eq(group.getId()))).thenReturn(group);
        when(fhirResource.getResource(UUID.fromString(resourceId), ResourceType.Patient)).thenReturn(patientJson);
        when(fhirResource.updateFhirObject(
            any(Patient.class), eq(fhirLink.getResourceId()), eq(fhirLink.getVersionId()))).thenReturn(patientJson);

        patientService.update(patient.getId(), group.getId(), fhirPatient);
    }
}
