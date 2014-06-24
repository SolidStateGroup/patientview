package org.patientview.api.service;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.patientview.api.config.TestCommonConfig;
import org.patientview.api.controller.model.Email;
import org.patientview.api.service.impl.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestCommonConfig.class, EmailServiceImpl.class})
public class EmailServiceTest {

    @Autowired
    private EmailServiceImpl emailService;

    @Ignore("Cannot currently load host/username/password from application.properties, see TestCommonConfig")
    @Test
    public void testSendEmail() {

        Email email = new Email();
        email.setBody("test body");
        email.setRecipients(new String[]{"test@solidstategroup.com"});
        email.setSubject("test subject");
        email.setSender("no-reply@solidstategroup.com");
        Assert.assertTrue("should have sent email", emailService.sendEmail(email));
    }
}
