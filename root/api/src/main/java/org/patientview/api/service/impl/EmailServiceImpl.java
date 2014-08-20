package org.patientview.api.service.impl;

import org.patientview.api.controller.model.Email;
import org.patientview.api.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
@Service
public class EmailServiceImpl extends AbstractServiceImpl<EmailServiceImpl> implements EmailService {

    protected final Logger LOG = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Inject
    private JavaMailSenderImpl javaMailSender;

    public boolean sendEmail(Email email) {
        // set email content
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(email.getSender());
        msg.setTo(email.getRecipients());
        msg.setSubject(email.getSubject());
        msg.setText(email.getBody());

        try {
            javaMailSender.send(msg);
            LOG.info("Sent email to {}", msg.getTo()[0]);
            return true;
        } catch (MailException ex) {
            LOG.error("Unable to send message", ex);
            return true;
        }
    }
}
