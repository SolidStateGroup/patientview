package org.patientview.api.service;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.FoodDiaryServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FoodDiary;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.FoodDiaryRepository;
import org.patientview.persistence.repository.IdentifierRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 21/12/2015
 */
public class FoodDiaryServiceTest {

    User creator;

    @Mock
    FoodDiaryRepository foodDiaryRepository;

    @InjectMocks
    FoodDiaryService foodDiaryService = new FoodDiaryServiceImpl();

    @Mock
    IdentifierRepository identifierRepository;

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
    public void testAdd() throws ResourceNotFoundException {
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        FoodDiary foodDiary = new FoodDiary(user, "red meat", new Date());

        foodDiaryService.add(user.getId(), foodDiary);
        verify(foodDiaryRepository, times(1)).save(eq(foodDiary));
    }

    @Test
    public void testGet() throws ResourceNotFoundException {
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        List<FoodDiary> foodDiaries = foodDiaryService.get(user.getId());
        Assert.assertEquals("Should return 0 food diaries", 0, foodDiaries.size());
    }

    @Test
    public void testDelete() throws ResourceNotFoundException, ResourceForbiddenException {
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        // food diary
        FoodDiary foodDiary = new FoodDiary(user, "red meat", new Date());

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(foodDiaryRepository.findById(eq(foodDiary.getId()))).thenReturn(Optional.of(foodDiary));

        foodDiaryService.delete(user.getId(), foodDiary.getId());
        verify(foodDiaryRepository, times(1)).delete(eq(foodDiary));
    }

    @Test
    public void testMigrate() throws IOException {
        User user = TestUtils.createUser("testUser");
        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        List<Identifier> identifiers = new ArrayList<>();
        identifiers.add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.GLOBAL_ADMIN);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        when(identifierRepository.findByValue(eq(identifier.getIdentifier()))).thenReturn(identifiers);
        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));

        String csv = "1,foods,comment,1111111111,2015-05-23 00:00:00\n"
                + "1,foods2,comment2,1111111111,2015-05-23 00:00:00\n"
                + "1,foods,comment,2222222222,2015-05-23 00:00:00";

        MultipartFile file = new MockMultipartFile("file", csv.getBytes());

        foodDiaryService.migrate(file);
        verify(foodDiaryRepository, times(2)).save(any(FoodDiary.class));
    }

    @Test
    public void testUpdate() throws ResourceNotFoundException, ResourceForbiddenException {
        User user = TestUtils.createUser("testUser");
        user.setId(1L);
        user.setIdentifiers(new HashSet<Identifier>());

        Group group = TestUtils.createGroup("testGroup");
        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        // user and security
        Role role = TestUtils.createRole(RoleName.PATIENT);
        user.setId(1L);
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        // food diary
        Date now = new Date();
        FoodDiary foodDiary = new FoodDiary(user, "red meat", now);
        foodDiary.setId(1L);
        FoodDiary newFoodDiary = new FoodDiary(user, "pink meat", now);
        newFoodDiary.setId(1L);

        when(userRepository.findById(eq(user.getId()))).thenReturn(Optional.of(user));
        when(foodDiaryRepository.findById(eq(foodDiary.getId()))).thenReturn(Optional.of(foodDiary));
        when(foodDiaryRepository.save(eq(newFoodDiary))).thenReturn(newFoodDiary);

        FoodDiary updated = foodDiaryService.update(user.getId(), newFoodDiary);

        Assert.assertNotNull("Should return updated FoodDiary", updated);
        Assert.assertEquals("Should have updated food", newFoodDiary.getFood(), updated.getFood());
        verify(foodDiaryRepository, times(1)).save(any(FoodDiary.class));
    }
}

