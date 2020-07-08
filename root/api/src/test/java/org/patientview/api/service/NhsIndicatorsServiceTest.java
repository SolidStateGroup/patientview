package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.model.NhsIndicators;
import org.patientview.api.service.impl.NhsIndicatorsServiceImpl;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.FhirLinkRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.persistence.resource.FhirResource;
import org.patientview.test.util.TestUtils;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/09/2016
 */
public class NhsIndicatorsServiceTest {

    User creator;

    @Mock
    CodeRepository codeRepository;

    @Mock
    FhirLinkRepository fhirLinkRepository;

    @Mock
    FhirResource fhirResource;

    @Mock
    GroupRepository groupRepository;

    @Mock
    LookupRepository lookupRepository;

    @InjectMocks
    NhsIndicatorsService nhsIndicatorsService = new NhsIndicatorsServiceImpl();

    @Mock
    Query query;

    @Mock
    UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Test
    @Ignore("Temporary while testing methods for datasource are explored")
    public void testGetNhsIndicators() throws Exception {
        Group group = TestUtils.createGroup("testGroup");

        // user and security
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        List<FhirLink> fhirLinks = new ArrayList<>();
        fhirLinks.add(new FhirLink(1L, UUID.randomUUID(), new User()));
        List<UUID> uuids = new ArrayList<>();
        uuids.add(fhirLinks.get(0).getResourceId());

        Code code = new Code();
        code.setCode("TP");
        List<Code> foundCodes = new ArrayList<>(Arrays.asList(code));

        Long tpCount = 20L;

        List<Group> groups = new ArrayList<>(Arrays.asList(group));
        List<Long> userIds = Arrays.asList(100L, 200L);
        List<Long> userIdsRecentLogin = Arrays.asList(100L);

        when(codeRepository.findAllByCodes(any(List.class))).thenReturn(foundCodes);
        when(fhirLinkRepository.findByUserIdsAndGroups(eq(userIds), eq(groups))).thenReturn(fhirLinks);
        when(fhirLinkRepository.findByUserIdsAndGroupsAndRecentLogin(
                eq(userIdsRecentLogin), eq(groups), any(Date.class))).thenReturn(fhirLinks);
        when(fhirResource.getCountEncounterBySubjectIdsAndCodes(eq(uuids), any(List.class))).thenReturn(tpCount);
        when(groupRepository.findById(eq(group.getId()))).thenReturn(Optional.of(group));
        when(userRepository.findPatientUserIds(eq(group.getId()))).thenReturn(userIds);
        when(userRepository.findPatientUserIdsByRecentLogin(eq(group.getId()), any(Date.class)))
                .thenReturn(userIdsRecentLogin);

        NhsIndicators nhsIndicators = nhsIndicatorsService.getNhsIndicators(group.getId());

        assertEquals("Should have correct Group ID", group.getId(), nhsIndicators.getGroupId());
        assertTrue("Should have at least one Code in codeMap",
                nhsIndicators.getData().getIndicatorCodeMap().get("Transplant").size() > 0);
        assertEquals("Should have correct Code in codeMap",
                code.getCode(), nhsIndicators.getData().getIndicatorCodeMap().get("Transplant").get(0));
        assertEquals("Should have correct count for Code in codeCount",
                tpCount, nhsIndicators.getData().getIndicatorCount().get("Transplant"));

        verify(codeRepository, Mockito.atLeastOnce()).findAllByCodes(any(List.class));
        verify(groupRepository, Mockito.times(1)).findById(eq(group.getId()));
        verify(fhirLinkRepository, Mockito.times(1)).findByUserIdsAndGroups(eq(userIds), eq(groups));
        verify(fhirLinkRepository, Mockito.times(1)).findByUserIdsAndGroupsAndRecentLogin(
                eq(userIdsRecentLogin), eq(groups), any(Date.class));
        verify(fhirResource, Mockito.times(10)).getCountEncounterBySubjectIdsAndCodes(eq(uuids), any(List.class));
        verify(fhirResource, Mockito.times(2)).getCountEncounterBySubjectIdsAndNotCodes(eq(uuids), any(List.class));
        verify(userRepository, Mockito.times(1)).findPatientUserIds(eq(group.getId()));
        verify(userRepository, Mockito.times(1)).findPatientUserIdsByRecentLogin(eq(group.getId()), any(Date.class));
    }
}
