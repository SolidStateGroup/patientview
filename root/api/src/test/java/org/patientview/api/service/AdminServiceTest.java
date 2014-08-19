package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.AdminServiceImpl;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupFeature;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupFeatureRepository;
import org.patientview.persistence.repository.GroupRelationshipRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupRoleRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
public class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private LookupRepository lookupRepository;

    @Mock
    private GroupFeatureRepository groupFeatureRepository;

    @Mock
    private FeatureRepository featureRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private GroupRoleRepository groupRoleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private GroupRelationshipRepository groupRelationshipRepository;

    @InjectMocks
    private AdminService adminService = new AdminServiceImpl();

    private User creator;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    /**
     * Test: The creation of the feature linked to a group.
     * Fail: The feature is not linked to the group.
     *
     */
    @Test
    public void testAddGroupFeature() {
        when(userRepository.findOne(Matchers.anyLong())).thenReturn(new User());
        when(groupRepository.findOne(Matchers.anyLong())).thenReturn(new Group());
        when(featureRepository.findOne(Matchers.anyLong())).thenReturn(new Feature());

        // Return a group feature
        when(groupFeatureRepository.save(Matchers.any(GroupFeature.class))).thenReturn(new GroupFeature());

        // Call
        GroupFeature groupFeature = adminService.addGroupFeature(1L, 1L);

        // Test
        verify(groupFeatureRepository, Mockito.times(1)).save(Matchers.any(GroupFeature.class));
        Assert.assertNotNull("A group feature has been created", groupFeature);
    }


}
