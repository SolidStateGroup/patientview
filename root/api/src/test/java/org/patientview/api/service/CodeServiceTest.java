package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.CodeServiceImpl;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.test.util.TestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/06/2016
 */
public class CodeServiceTest {

    @Mock
    CodeRepository codeRepository;

    @InjectMocks
    CodeService codeService = new CodeServiceImpl();

    @Mock
    NhsChoicesService nhsChoicesService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    /**
     * Test to check NHS Choices is called to updated introduction url (link on Code) and description
     * @throws Exception
     */
    @Test
    public void testGet_PATIENTVIEW() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        Code code = new Code();
        code.setCodeType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.CODE_TYPE),
                CodeTypes.DIAGNOSIS.toString()));
        code.setStandardType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.CODE_STANDARD),
                "PATIENTVIEW"));
        code.setCode("code");
        code.setId(1L);

        when(codeRepository.findOne(eq(code.getId()))).thenReturn(code);

        Code foundCode = codeService.get(code.getId());


        Assert.assertNotNull("The returned Code should not be null", foundCode);

        verify(codeRepository, Mockito.times(1)).findOne(eq(code.getId()));
        verify(nhsChoicesService, Mockito.times(1)).setIntroductionUrl(eq(code.getCode()));
        verify(nhsChoicesService, Mockito.times(1)).setDescription(eq(code.getCode()));
    }
}
