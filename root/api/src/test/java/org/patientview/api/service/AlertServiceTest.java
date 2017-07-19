package org.patientview.api.service;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.client.FirebaseClient;
import org.patientview.api.model.ContactAlert;
import org.patientview.api.model.ImportAlert;
import org.patientview.api.service.impl.AlertServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.FeatureType;
import org.patientview.persistence.model.enums.GroupTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.RoleType;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.AuditRepository;
import org.patientview.persistence.repository.FeatureRepository;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.RoleRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/01/2015
 */
public class AlertServiceTest {

    User creator;

    @Mock
    AuditRepository auditRepository;

    @Mock
    AlertRepository alertRepository;

    @InjectMocks
    AlertService alertService = new AlertServiceImpl();

    @Mock
    FeatureRepository featureRepository;

    @Mock
    EmailService emailService;

    @Mock
    GroupRepository groupRepository;

    @Mock
    GroupService groupService;

    @Mock
    Properties properties;

    @Mock
    RoleRepository roleRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    private FirebaseClient notificationClient;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @After
    public void tearDown() {
        TestUtils.removeAuthentication();
    }

    @Ignore("Security issue on live deploy, can be tested locally")
    @Test
    public void testGetContactAlerts() throws ResourceNotFoundException {
        Group group = TestUtils.createGroup("GROUP1");
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.GROUP);
        Lookup type = TestUtils.createLookup(lookupType, GroupTypes.UNIT.toString());
        group.setGroupType(type);
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        List<Role> roles = new ArrayList<>();
        roles.add(role);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        List<User> users = new ArrayList<>();
        users.add(user);
        PageImpl<User> userPage = new PageImpl<>(users);

        List<org.patientview.api.model.Group> groupList = new ArrayList<>();
        groupList.add(new org.patientview.api.model.Group(group));
        PageImpl<org.patientview.api.model.Group> groups = new PageImpl<>(groupList);

        Feature feature1 = TestUtils.createFeature(FeatureType.DEFAULT_MESSAGING_CONTACT.toString());
        Feature feature2 = TestUtils.createFeature(FeatureType.PATIENT_SUPPORT_CONTACT.toString());
        Feature feature3 = TestUtils.createFeature(FeatureType.UNIT_TECHNICAL_CONTACT.toString());

        when(groupService.getUserGroups(eq(user.getId()), any(GetParameters.class))).thenReturn(groups);
        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        when(roleRepository.findByRoleType(eq(RoleType.STAFF))).thenReturn(roles);
        when(featureRepository.findByName(eq(FeatureType.DEFAULT_MESSAGING_CONTACT.toString()))).thenReturn(feature1);
        when(featureRepository.findByName(eq(FeatureType.PATIENT_SUPPORT_CONTACT.toString()))).thenReturn(feature2);
        when(featureRepository.findByName(eq(FeatureType.UNIT_TECHNICAL_CONTACT.toString()))).thenReturn(feature3);
        when(userRepository.findStaffByGroupsRolesFeatures(
                eq("%%"), any(List.class), any(List.class), any(List.class), any(Pageable.class)))
                .thenReturn(userPage);

        List<ContactAlert> contactAlerts = alertService.getContactAlerts(user.getId());
        Assert.assertEquals("Should return 0 contact alerts", 0, contactAlerts.size());
    }

    @Ignore("Security issue on live deploy, can be tested locally")
    @Test
    public void testGetImportAlerts() throws ResourceNotFoundException {
        Group group = TestUtils.createGroup("GROUP1");
        LookupType lookupType = TestUtils.createLookupType(LookupTypes.GROUP);
        Lookup type = TestUtils.createLookup(lookupType, GroupTypes.UNIT.toString());
        group.setGroupType(type);
        Role role = TestUtils.createRole(RoleName.UNIT_ADMIN);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        List<Long> groupIds = new ArrayList<>();
        groupIds.add(group.getId());

        List<org.patientview.api.model.Group> userGroups = new ArrayList<>();
        userGroups.add(new org.patientview.api.model.Group(group));
        PageImpl<org.patientview.api.model.Group> groupPage = new PageImpl<>(userGroups);

        List<AuditActions> auditActions = new ArrayList<>();
        auditActions.add(AuditActions.PATIENT_DATA_FAIL);
        auditActions.add(AuditActions.PATIENT_DATA_VALIDATE_FAIL);

        List<Object[]> audits = new ArrayList<>();
        Object[] first = new Object[2];
        first[0] = group.getId();
        first[1] = 2L;
        Object[] second = new Object[2];
        second[0] = group.getId() + 1;
        second[1] = 20L;
        audits.add(first);
        audits.add(second);

        when(groupService.getUserGroups(eq(user.getId()), any(GetParameters.class))).thenReturn(groupPage);
        when(auditRepository.findAllByCountGroupAction(eq(groupIds), any(Date.class), eq(auditActions)))
                .thenReturn(audits);
        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);

        List<ImportAlert> importAlerts = alertService.getImportAlerts(user.getId());

        Assert.assertEquals("Should return 1 import alerts", 1, importAlerts.size());
        Assert.assertEquals("Should return correct group", group.getId(), importAlerts.get(0).getGroup().getId());
        Assert.assertNotNull("Should return count", importAlerts.get(0).getFailedImports());
        Assert.assertEquals("Should return correct count", Long.valueOf(2), importAlerts.get(0).getFailedImports());
    }

    @Test
    public void testRemoveAlert() throws ResourceNotFoundException, ResourceForbiddenException {

        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");

        Group group = TestUtils.createGroup("GROUP1");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Alert alert = new Alert();

        alert.setId(1L);
        alert.setObservationHeading(observationHeading);
        alert.setWebAlert(true);
        alert.setWebAlertViewed(false);
        alert.setEmailAlert(true);
        alert.setEmailAlertSent(false);
        alert.setUser(user);
        alert.setAlertType(AlertTypes.RESULT);

        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        when(alertRepository.findOne(
                eq(alert.getId()))).thenReturn(alert);

        alertService.removeAlert(user.getId(), alert.getId());
        verify(alertRepository, Mockito.times(1)).delete(any(Alert.class));
    }

    @Test
    public void testUpdateAlert() throws ResourceNotFoundException, ResourceForbiddenException {

        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");

        Group group = TestUtils.createGroup("GROUP1");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);
        Set<GroupRole> groupRoles = new HashSet<>();
        groupRoles.add(groupRole);
        TestUtils.authenticateTest(user, groupRoles);

        Alert alert = new Alert();

        alert.setId(1L);
        alert.setObservationHeading(observationHeading);
        alert.setWebAlert(true);
        alert.setWebAlertViewed(false);
        alert.setEmailAlert(true);
        alert.setEmailAlertSent(false);
        alert.setUser(user);
        alert.setAlertType(AlertTypes.RESULT);

        org.patientview.api.model.Alert apiAlert = new org.patientview.api.model.Alert(alert, user);

        when(userRepository.findOne(eq(user.getId()))).thenReturn(user);
        when(alertRepository.findOne(eq(alert.getId()))).thenReturn(alert);

        alertService.updateAlert(user.getId(), apiAlert);
        verify(alertRepository, Mockito.times(1)).save(any(Alert.class));
    }

    @Test
    public void testSendAlertEmails() throws Exception {

        User user = TestUtils.createUser("testUser");
        user.setEmail("test@solidstategroup.com");

        Alert alert = new Alert();
        alert.setEmailAlertSent(false);
        alert.setEmailAlert(true);
        alert.setUser(user);
        alert.setId(1L);
        alert.setAlertType(AlertTypes.RESULT);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);

        when(alertRepository.findByEmailAlertSetAndNotSent()).thenReturn(alerts);
        when(alertRepository.findOne(eq(alert.getId()))).thenReturn(alert);
        alertService.sendAlertEmails();

        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));
        verify(alertRepository, Mockito.times(1)).save(any(Alert.class));
    }

    @Test
    public void testSendMultipleAlertEmails() throws Exception {

        User user = TestUtils.createUser("testUser");
        user.setEmail("test@solidstategroup.com");

        Alert alert = new Alert();
        alert.setEmailAlertSent(false);
        alert.setEmailAlert(true);
        alert.setUser(user);
        alert.setId(1L);
        alert.setAlertType(AlertTypes.RESULT);

        User user2 = TestUtils.createUser("test2User");
        user2.setEmail("test2@solidstategroup.com");

        Alert alert2 = new Alert();
        alert2.setEmailAlertSent(false);
        alert2.setEmailAlert(true);
        alert2.setUser(user);
        alert2.setId(2L);
        alert2.setAlertType(AlertTypes.RESULT);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);
        alerts.add(alert2);

        when(alertRepository.findByEmailAlertSetAndNotSent()).thenReturn(alerts);
        when(alertRepository.findOne(eq(alert.getId()))).thenReturn(alert);
        when(alertRepository.findOne(eq(alert2.getId()))).thenReturn(alert);
        alertService.sendAlertEmails();

        verify(emailService, Mockito.times(1)).sendEmail(any(Email.class));
        verify(alertRepository, Mockito.times(2)).save(any(Alert.class));
    }

    @Test
    public void testSendMultipleIndividualAlertEmails() throws Exception {

        User user = TestUtils.createUser("testUser");
        user.setEmail("test@solidstategroup.com");

        Alert alert = new Alert();
        alert.setEmailAlertSent(false);
        alert.setEmailAlert(true);
        alert.setUser(user);
        alert.setId(1L);
        alert.setAlertType(AlertTypes.RESULT);

        User user2 = TestUtils.createUser("test2User");
        user2.setEmail("test2@solidstategroup.com");

        Alert alert2 = new Alert();
        alert2.setEmailAlertSent(false);
        alert2.setEmailAlert(true);
        alert2.setUser(user2);
        alert2.setId(2L);
        alert2.setAlertType(AlertTypes.RESULT);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);
        alerts.add(alert2);

        when(alertRepository.findByEmailAlertSetAndNotSent()).thenReturn(alerts);
        when(alertRepository.findOne(eq(alert.getId()))).thenReturn(alert);
        when(alertRepository.findOne(eq(alert2.getId()))).thenReturn(alert2);
        alertService.sendIndividualAlertEmails();

        verify(emailService, Mockito.times(2)).sendEmail(any(Email.class));
        verify(alertRepository, Mockito.times(2)).save(any(Alert.class));
    }

    @Test
    public void testPushNotifications() throws Exception {

        User user = TestUtils.createUser("testUser");
        user.setEmail("test@solidstategroup.com");

        Alert alert = new Alert();
        alert.setMobileAlertSent(false);
        alert.setMobileAlert(true);
        alert.setUser(user);
        alert.setId(1L);
        alert.setAlertType(AlertTypes.RESULT);

        User user2 = TestUtils.createUser("test2User");
        user2.setEmail("test2@solidstategroup.com");

        Alert alert2 = new Alert();
        alert2.setMobileAlertSent(false);
        alert2.setMobileAlert(true);
        alert2.setUser(user2);
        alert2.setId(2L);
        alert2.setAlertType(AlertTypes.RESULT);

        List<Alert> alerts = new ArrayList<>();
        alerts.add(alert);
        alerts.add(alert2);

        when(alertRepository.findByMobileAlertSetAndNotSent()).thenReturn(alerts);

        alertService.pushNotifications();

        verify(notificationClient, Mockito.times(2)).push(any(Long.class));
        verify(alertRepository, Mockito.times(2)).save(any(Alert.class));
    }
}

