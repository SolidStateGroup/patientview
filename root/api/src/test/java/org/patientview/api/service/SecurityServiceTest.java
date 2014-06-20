package org.patientview.api.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.SecurityServiceImpl;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.NewsItemRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.RouteRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests concerning the test of the user security service for retrieving data that the user has access too.
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
public class SecurityServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NewsItemRepository newsItemRepository;

    @InjectMocks
    private SecurityService securityService = new SecurityServiceImpl();


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    /**
     * Test: To see if the news is return by single group OR role
     * Fail: The calls to the repository are not made
     */
    @Test
    public void testGetNewsByUser() {

        User testUser = TestUtils.createUser(23L, "testUser");
        when(userRepository.findOne(Matchers.anyLong())).thenReturn(testUser);

        securityService.getNewsByUser(testUser.getId());

        verify(newsItemRepository, Mockito.times(1)).getGroupNewsByUser(Matchers.eq(testUser));
        verify(newsItemRepository, Mockito.times(1)).getRoleNewsByUser(Matchers.eq(testUser));
    }


}
