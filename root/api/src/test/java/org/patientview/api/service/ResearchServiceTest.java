package org.patientview.api.service;

import generated.Sex;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.Patient;
import org.patientview.api.service.impl.NewsServiceImpl;
import org.patientview.api.service.impl.ResearchServiceImpl;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirPatient;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.NewsItem;
import org.patientview.persistence.model.NewsLink;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.ResearchStudyRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResearchServiceTest {

    User creator;

    @Mock
    ResearchStudyRepository researchStudyRepository;

    @Mock
    ApiPatientService apiPatientService;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ResearchService researchService = new ResearchServiceImpl();


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test: To see if the research Study is returned for a user
     * Fail: The calls to the repository are not made, not the right number, not in right order
     */
    @Test
    public void testGetAllForUser() throws FhirResourceException, ResourceForbiddenException, ResourceNotFoundException {
        FhirPatient fhirPatient = new FhirPatient();
        fhirPatient.setGender(Sex.MALE.toString());

        Group group = new Group();
        group.setId(1L);
        group.setCode("GROUP1");

        GroupRole groupRole = new GroupRole();
        groupRole.setId(1L);
        groupRole.setGroup(group);

        HashSet<GroupRole> groupRoleSet = new HashSet<>();
        groupRoleSet.add(groupRole);

        // code (diagnosis)
        Code code = TestUtils.createCode("Crohn's Disease");
        code.setCodeType(TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_TYPE), DiagnosisTypes.DIAGNOSIS.toString()));
        code.setCode("CD");


        // user
        User user = TestUtils.createUser("patient");
        user.setDateOfBirth(new Date(642380400000L));



        Role patientRole = TestUtils.createRole(RoleName.PATIENT);
        GroupRole groupRolePatient = TestUtils.createGroupRole(patientRole, group, user);
        Set<GroupRole> groupRolesPatient = new HashSet<>();
        groupRolesPatient.add(groupRolePatient);
        user.setGroupRoles(groupRolesPatient);

        // identifier
        Identifier identifier = TestUtils.createIdentifier(
                TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                        IdentifierTypes.NHS_NUMBER.toString()), user, "1111111111");
        user.getIdentifiers().add(identifier);

        // fhir links
        FhirLink fhirLink = TestUtils.createFhirLink(user, identifier);
        user.setFhirLinks(new HashSet<FhirLink>());
        user.getFhirLinks().add(fhirLink);

        Patient patient = new Patient();
        patient.setFhirPatient(fhirPatient);

        FhirEncounter encounter = new FhirEncounter();
        encounter.setEncounterType(EncounterTypes.TREATMENT.toString());
        ArrayList<FhirEncounter> encounters = new ArrayList<>();
        encounters.add(encounter);
        patient.setFhirEncounters(encounters);

        ArrayList<Patient> patients = new ArrayList<>();
        patients.add(patient);



        when(userRepository.findOne(user.getId())).thenReturn(user);
        when(apiPatientService.get(user.getId(), null)).thenReturn(patients);
    }

}
