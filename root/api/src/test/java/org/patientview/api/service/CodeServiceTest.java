package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.BaseCode;
import org.patientview.api.service.impl.CodeServiceImpl;
import org.patientview.persistence.model.Category;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeCategory;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeStandardTypes;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CategoryRepository;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.test.util.TestUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    CategoryRepository categoryRepository;

    @Mock
    CodeRepository codeRepository;

    @InjectMocks
    CodeService codeService = new CodeServiceImpl();

    @Mock
    LookupRepository lookupRepository;

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
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
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
                CodeStandardTypes.PATIENTVIEW.toString()));
        code.setCode("code");
        code.setId(1L);

        when(codeRepository.findOne(eq(code.getId()))).thenReturn(code);

        Code foundCode = codeService.get(code.getId());

        Assert.assertNotNull("The returned Code should not be null", foundCode);

        verify(codeRepository, Mockito.times(1)).findOne(eq(code.getId()));
        verify(nhsChoicesService, Mockito.times(1)).updateCodeData(eq(code));
    }

    @Test
    public void testGetAllDiagnosisCodes() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.SPECIALTY_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        LookupType lookupType = TestUtils.createLookupType(LookupTypes.CODE_TYPE);
        Lookup lookup = TestUtils.createLookup(lookupType, CodeTypes.DIAGNOSIS.toString());

        Code code = new Code();
        code.setCodeType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.CODE_TYPE),
                CodeTypes.DIAGNOSIS.toString()));
        code.setStandardType(TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.CODE_STANDARD),
                CodeStandardTypes.PATIENTVIEW.toString()));
        code.setCode("code");
        code.setId(1L);

        when(lookupRepository.findByTypeAndValue(eq(LookupTypes.CODE_TYPE), eq(CodeTypes.DIAGNOSIS.toString())))
                .thenReturn(lookup);
        when(codeRepository.findAllByType(eq(lookup))).thenReturn(Arrays.asList(code));

        List<BaseCode> diagnosisCodes = codeService.getAllDiagnosisCodes();

        Assert.assertFalse("The returned List of BaseCode should not be empty", diagnosisCodes.isEmpty());
        Assert.assertEquals("Should have the correct Code code", diagnosisCodes.get(0).getCode(), code.getCode());
        Assert.assertEquals("Should have the correct Code description",
                diagnosisCodes.get(0).getDescription(), code.getDescription());

        verify(lookupRepository, Mockito.times(1)).findByTypeAndValue(eq(LookupTypes.CODE_TYPE),
                eq(CodeTypes.DIAGNOSIS.toString()));
        verify(codeRepository, Mockito.times(1)).findAllByType(eq(lookup));
    }

    @Test
    public void testGetByCategory() throws Exception {
        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
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
                CodeStandardTypes.PATIENTVIEW.toString()));
        code.setCode("code");
        code.setId(1L);

        Category category = new Category(1, "icd10", "friendly");
        category.setId(1L);

        CodeCategory codeCategory = new CodeCategory(code, category);
        category.getCodeCategories().add(codeCategory);

        when(categoryRepository.findOne(eq(category.getId()))).thenReturn(category);

        List<BaseCode> foundCodes = codeService.getByCategory(category.getId());

        Assert.assertNotNull("The returned BaseCodes should not be null", foundCodes);
        Assert.assertEquals("Should be 1 returned BaseCode", 1, foundCodes.size());
        Assert.assertEquals("Should be correct returned BaseCode", code.getCode(), foundCodes.get(0).getCode());

        verify(categoryRepository, Mockito.times(1)).findOne(eq(category.getId()));
    }
}
