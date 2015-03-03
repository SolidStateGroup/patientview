package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.config.TestCommonConfig;
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
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.test.util.TestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.when;

/**
 * Tests for PatientService, used for reading and writing patient record in FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 03/03/2015
 */
public class PatientServiceTest {
    
    @InjectMocks
    PatientService patientService = new PatientServiceImpl();

    @Mock
    private CodeService codeService;

    @Mock
    private ConditionService conditionService;

    private User creator;
    
    @Mock
    private EncounterService encounterService;

    @Mock
    private DiagnosticService diagnosticService;

    @Mock
    private FhirLinkService fhirLinkService;

    @Mock
    private FhirResource fhirResource;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupService groupService;

    @Mock
    private IdentifierRepository identifierRepository;

    @Mock
    private IdentifierService identifierService;

    @Mock
    private LetterService letterService;

    @Mock
    private LookupService lookupService;

    @Mock
    private MedicationService medicationService;

    @Mock
    private ObservationHeadingService observationHeadingService;

    @Mock
    private ObservationService observationService;

    @Mock
    private PractitionerService practitionerService;

    @Mock
    private UserService userService;

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
        fhirLink.setResourceId(UUID.fromString("d52847eb-c2c7-4015-ba6c-952962536287"));
        fhirLink.setActive(true);
        patient.getFhirLinks().add(fhirLink);

        // object containing updated data
        FhirPatient fhirPatient = new FhirPatient();
        fhirPatient.setForename("forename");
        fhirPatient.setSurname("surname");
        
        when(userRepository.findOne(Matchers.eq(patient.getId()))).thenReturn(patient);
        
        patientService.update(patient.getId(), group.getId(), fhirPatient);
    }
}
