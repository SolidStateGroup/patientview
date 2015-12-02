package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.ConditionServiceImpl;
import org.patientview.persistence.model.Code;
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
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/12/2015
 */
public class ConditionServiceTest {

    @Mock
    CodeService codeService;

    @Mock
    FhirLinkRepository fhirLinkRepository;

    @Mock
    GroupService groupService;

    @Mock
    LookupService lookupService;

    @Mock
    UserService userService;

    @InjectMocks
    ConditionService conditionService = new ConditionServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testStaffAddCondition() throws Exception {
        String code = "00";

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

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
        Code codeEntity = TestUtils.createCode("00");
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
        when(userService.get(eq(patient.getId()))).thenReturn(patient);
        when(userService.currentUserCanGetUser(eq(patient))).thenReturn(true);

        conditionService.staffAddCondition(patient.getId(), "00");

    }
}
