package org.patientview.test.util;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRelationship;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.RoleType;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.Roles;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Test utilities for testing with a Persistence Context.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Service
public class DataTestUtils {

    @Inject
    UserRepository userRepository;

    @Inject
    LookupRepository lookupRepository;

    @Inject
    LookupTypeRepository lookupTypeRepository;

    @Inject
    FeatureRepository featureRepository;

    @Inject
    RoleRepository roleRepository;

    @Inject
    GroupRepository groupRepository;

    @Inject
    GroupRoleRepository groupRoleRepository;

    @Inject
    GroupRelationshipRepository groupRelationshipRepository;

    public Lookup createLookup(String lookupName, String lookupTypeName, User creator) {

        LookupType lookupType = TestUtils.createLookupType(null, lookupTypeName, creator);
        lookupType.setCreator(creator);
        lookupTypeRepository.save(lookupType);

        Lookup lookupValue = TestUtils.createLookup(null,lookupType, lookupName,  creator);
        lookupValue.setCreator(creator);
        return lookupRepository.save(lookupValue);

    }

    public User createUser(String username) {
        User user = TestUtils.createUser(null, "testUser");
        return userRepository.save(user);
    }

    public Feature createFeature(String name, User creator) {

        Feature feature = TestUtils.createFeature(null, "TestFeature", creator);
        return featureRepository.save(feature);
    }

    public Role createRole(String name, User creator) {
        Role role = TestUtils.createRole(null, Roles.PATIENT, creator);

        RoleType roleType = new RoleType();
        roleType.getValue();
        return roleRepository.save(role);
    }

    public Group createGroup(String name, User creator) {
        Group group = TestUtils.createGroup(null, "TEST_GROUP", creator);
        return groupRepository.save(group);
    }


    public GroupRole createGroupRole(User user, Group group, Role role, User creator) {
        GroupRole groupRole = TestUtils.createGroupRole(null, role, group, user, creator);
        return groupRoleRepository.save(groupRole);
    }

    public GroupRelationship createGroupRelationship(Group source, Group object, Lookup relationshipType, User creator) {
        GroupRelationship groupRelationship = TestUtils.createGroupRelationship(null, source, object, relationshipType, creator);
        return groupRelationshipRepository.save(groupRelationship);
    }


}
