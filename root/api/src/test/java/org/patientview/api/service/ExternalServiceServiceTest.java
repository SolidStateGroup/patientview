package org.patientview.api.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.impl.ExternalServiceServiceImpl;
import org.patientview.persistence.model.ExternalServiceTaskQueueItem;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.ExternalServices;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.ExternalServiceTaskQueueItemRepository;
import org.patientview.test.util.TestUtils;

import java.util.Date;
import java.util.Properties;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
public class ExternalServiceServiceTest {

    @Mock
    private ExternalServiceTaskQueueItemRepository externalServiceTaskQueueItemRepository;

    @InjectMocks
    ExternalServiceServiceImpl externalServiceService;

    @Mock
    Properties properties;

    private User creator;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        creator = TestUtils.createUser("creator");
    }

    @Test
    public void testAddToQueue() {
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        String xml = "<Container><Patient><PatientNumbers><PatientNumber><Number>1111111111</Number>" +
                "<Organization>NHS_NUMBER</Organization></PatientNumber></PatientNumbers></Patient>" +
                "<ProgramMemberships><ProgramMembership><EnteredBy>testUser</EnteredBy><EnteredAt>PV2</EnteredAt>" +
                "<EnteredOn>Thu Apr 30 15:47:35 BST 2015</EnteredOn><ExternalId>422930</ExternalId>" +
                "<ProgramName>TESTGROUP2</ProgramName><ProgramDescription>testGroup2</ProgramDescription>" +
                "<FromTime>Thu Apr 30 15:30:55 BST 2015</FromTime><ToTime>Thu Apr 30 15:47:35 BST 2015</ToTime>" +
                "</ProgramMembership></ProgramMemberships></Container>";

        when(properties.getProperty("external.service.rdc.url")).thenReturn("http://localhost:8080/");
        when(properties.getProperty("external.service.rdc.method")).thenReturn("POST");

        Group group = TestUtils.createGroup("testGroup");
        Role role = TestUtils.createRole(RoleName.PATIENT);
        User user = TestUtils.createUser("testUser");
        GroupRole groupRole = TestUtils.createGroupRole(role, group, user);


        externalServiceService.addToQueue(ExternalServices.RDC_GROUP_ROLE_NOTIFICATION, xml, creator, new Date(),
                groupRole);

        verify(externalServiceTaskQueueItemRepository, Mockito.times(1)).save(any(ExternalServiceTaskQueueItem.class));
    }
}
