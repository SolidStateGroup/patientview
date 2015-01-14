package org.patientview.api.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.AlertServiceImpl;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Alert;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AlertTypes;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.AlertRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;

import java.util.ArrayList;
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
    UserRepository userRepository;

    @Mock
    AlertRepository alertRepository;

    @Mock
    EmailService emailService;

    @Mock
    Properties properties;

    @InjectMocks
    AlertService alertService = new AlertServiceImpl();

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
}

