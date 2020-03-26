package org.patientview.api.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.GpMedicationStatus;
import org.patientview.api.service.impl.GpMedicationServiceImpl;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserFeature;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/11/2014
 */
public class GpMedicationServiceTest {

    @InjectMocks
    GpMedicationService gpMedicationService = new GpMedicationServiceImpl();

    @Mock
    UserRepository userRepository;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    public void testGetGpMedicationStatusByUserId() throws ResourceNotFoundException {

        // user and security
        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");

        Feature gpMedicineFeature = TestUtils.createFeature(FeatureType.GP_MEDICATION.toString());

        group.setGroupFeatures(new HashSet<GroupFeature>());
        GroupFeature groupFeature = TestUtils.createGroupFeature(gpMedicineFeature, group);
        group.getGroupFeatures().add(groupFeature);

        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        user.setGroupRoles(groupRoles);
        TestUtils.authenticateTest(user, groupRoles);

        user.setIdentifiers(new HashSet<Identifier>());

        Lookup lookup = TestUtils.createLookup(TestUtils.createLookupType(LookupTypes.IDENTIFIER),
                IdentifierTypes.NHS_NUMBER.toString());
        Identifier identifier = TestUtils.createIdentifier(lookup, user, "1111111111");
        user.getIdentifiers().add(identifier);

        UserFeature userGpMedicineFeature = TestUtils.createUserFeature(gpMedicineFeature, user);
        userGpMedicineFeature.setOptInStatus(true);
        userGpMedicineFeature.setOptInDate(new Date());
        user.setUserFeatures(new HashSet<UserFeature>());
        user.getUserFeatures().add(userGpMedicineFeature);

        when(userRepository.findById(Matchers.eq(user.getId()))).thenReturn(Optional.of(user));

        GpMedicationStatus gpMedicationStatus = gpMedicationService.getGpMedicationStatus(user.getId());

        Assert.assertEquals("Should be opted in", true, gpMedicationStatus.getOptInStatus());
        Assert.assertEquals("Should be available", true, gpMedicationStatus.isAvailable());
    }
}
