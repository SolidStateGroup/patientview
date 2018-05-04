package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.NhsChoicesServiceImpl;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.NhschoicesCondition;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeStandardTypes;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.persistence.repository.NhschoicesConditionRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 07/06/2016
 */
public class NhsChoicesServiceTest {

    @Mock
    CodeRepository codeRepository;

    @Mock
    NhschoicesConditionRepository nhschoicesConditionRepository;

    @Mock
    LookupRepository lookupRepository;

    @Mock
    LookupTypeRepository lookupTypeRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    NhsChoicesService nhsChoicesService = new NhsChoicesServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testSynchroniseConditions() throws Exception {

        // user and security
        User testUser = TestUtils.createUser("testUser");
        Group testGroup = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        GroupRole groupRole = TestUtils.createGroupRole(role, testGroup, testUser);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(testUser, groupRoles);

        // code and standard types for PATIENTVIEW codes
        Lookup codeType = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_TYPE), CodeTypes.DIAGNOSIS.toString());
        Lookup standardType = TestUtils.createLookup(
                TestUtils.createLookupType(LookupTypes.CODE_STANDARD), CodeStandardTypes.PATIENTVIEW.toString());

        // existing Codes
        List<Code> existingCodes = new ArrayList<>();
        existingCodes.add(TestUtils.createCode("code1", "codeDescription1"));
        existingCodes.add(TestUtils.createCode("code2", "codeDescription2"));

        // existing NhschoicesCondition - code1 has been removed so should be marked as removed externally
        List<NhschoicesCondition> conditions = new ArrayList<>();
        conditions.add(new NhschoicesCondition("code2", "someName2", "someUri2"));

        // new NhschoicesCondition
        conditions.add(new NhschoicesCondition("code3", "someName3", "someUri3"));

        when(codeRepository.findAllByStandardType(eq(standardType))).thenReturn(existingCodes);
        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.CODE_TYPE), eq(codeType.getValue())))
                .thenReturn(codeType);
        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.CODE_STANDARD), eq(standardType.getValue())))
                .thenReturn(standardType);
        when(nhschoicesConditionRepository.findAll()).thenReturn(conditions);

        nhsChoicesService.synchroniseConditions();

        verify(codeRepository, Mockito.times(1)).findAllByStandardType(eq(standardType));
        verify(codeRepository, Mockito.times(1)).save(any(List.class));
        verify(lookupRepository, Mockito.times(1))
                .findByTypeAndValue(eq(LookupTypes.CODE_TYPE), eq(codeType.getValue()));
        verify(lookupRepository, Mockito.times(1))
                .findByTypeAndValue(eq(LookupTypes.CODE_STANDARD), eq(standardType.getValue()));
    }
}
