package org.patientview.api.aspect;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.annotation.AuditTrail;
import org.patientview.service.AuditService;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.test.util.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TODO test needs to be improved to test for return types
 *
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = false)
@ComponentScan(basePackages = {"org.patientview.api.aspect","org.patientview.api.service"})
public class AuditTrailAspectTest {

    private static final Logger LOG = LoggerFactory.getLogger(AuditTrailAspectTest.class);

    private User creator;

    @Mock
    AuditService auditService;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuditAspect auditAspect = AuditAspect.aspectOf();

    @Before
    public void setUp() throws Exception {
        creator = TestUtils.createUser("creator");

        MockitoAnnotations.initMocks(this);
    }

    @AuditTrail(AuditActions.PASSWORD_CHANGE)
    public void annotatedObjectMethod(User user) {
        LOG.info("Executed object annotated method");
    }

    @AuditTrail(value = AuditActions.PASSWORD_RESET, objectType =  User.class)
    public void annotatedIdMethod(Long userId) {
        LOG.info("Executed Id method");
    }

    /**
     * Test: See if the creation of an audit object works
     * Fail: The audit repository is not touched
     *
     **/
    @Test
    public void testAuditChangePassword() {
        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user);

        when(userRepository.findById(Matchers.eq(user.getId()))).thenReturn(Optional.of(user));

        annotatedObjectMethod(user);
        // when create a user and then hit our test method
        verify(auditService, Mockito.times(2)).save(any(org.patientview.persistence.model.Audit.class));
    }

    /**
     * Test: See if the audit work with a Id being passed
     * Fail: The auditRepository is not accessed
     *
     **/
    @Test
    public void testAuditWithAnId() {
        User user = TestUtils.createUser("testUser");
        TestUtils.authenticateTest(user);

        when(userRepository.findById(Matchers.eq(user.getId()))).thenReturn(Optional.of(user));

        annotatedIdMethod(user.getId());
        verify(auditService, Mockito.times(2)).save(any(org.patientview.persistence.model.Audit.class));
    }
}
