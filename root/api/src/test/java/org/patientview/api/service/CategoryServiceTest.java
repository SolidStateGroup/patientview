package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.CategoryServiceImpl;
import org.patientview.persistence.model.Category;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeCategory;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeStandardTypes;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CategoryRepository;
import org.patientview.test.util.TestUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 20/06/2016
 */
public class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryService categoryService = new CategoryServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testDelete() throws Exception {
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

        Category category = new Category(1, "icd10", "friendly");
        category.setId(1L);

        CodeCategory codeCategory = new CodeCategory(code, category);
        category.getCodeCategories().add(codeCategory);

        when(categoryRepository.findById(eq(category.getId()))).thenReturn(Optional.of(category));

        categoryService.delete(category.getId());

        verify(categoryRepository, Mockito.times(1)).delete(eq(category));
    }
}
