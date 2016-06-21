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
import org.patientview.service.impl.FhirLinkServiceImpl;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.service.FhirLinkService;
import org.patientview.service.PatientService;
import org.patientview.test.util.TestUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/03/2016
 */
public class FhirLinkServiceTest {

    @InjectMocks
    private FhirLinkService fhirLinkService = new FhirLinkServiceImpl();

    @Mock
    private FhirResource fhirResource;

    @Mock
    private PatientService patientService;

    @Mock
    private FhirLinkRepository fhirLinkRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testCreateFhirLink() throws Exception {
        // auth
        Group group = TestUtils.createGroup("testGroup");
        Role staffRole = TestUtils.createRole(RoleName.UNIT_ADMIN);
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

        // built fhir patient
        Patient builtPatient = new Patient();

        // created fhir patient
        FhirDatabaseEntity fhirPatient = new FhirDatabaseEntity();
        fhirPatient.setLogicalId(UUID.randomUUID());

        when(fhirResource.createEntity(eq(builtPatient), eq(ResourceType.Patient.name()),
                eq("patient"))).thenReturn(fhirPatient);
        when(patientService.buildPatient(eq(patient), eq(identifier))).thenReturn(builtPatient);

        FhirLink fhirLink = fhirLinkService.createFhirLink(patient, identifier, group);

        Assert.assertNotNull("Should have created FhirLink", fhirLink);
        Assert.assertEquals("Should have correct identifier", fhirLink.getIdentifier().getIdentifier(),
                identifier.getIdentifier());
        Assert.assertEquals("Should have correct username", fhirLink.getUser().getUsername(),
                patient.getUsername());

        verify(fhirResource, times(1)).createEntity(eq(builtPatient), eq(ResourceType.Patient.name()),
                eq("patient"));
        verify(patientService, times(1)).buildPatient(eq(patient), eq(identifier));
        verify(fhirLinkRepository, times(1)).save(any(FhirLink.class));
    }
}
