package org.patientview.api.service.impl;

import org.patientview.api.model.Email;
import org.patientview.api.service.EmailService;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
@Service
public class EmailServiceImpl extends AbstractServiceImpl<EmailServiceImpl> implements EmailService {

    @Inject
    private JavaMailSenderImpl javaMailSender;

    public boolean sendEmail(Email email) throws MailException {
        // set email content
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(email.getSender());
        msg.setTo(email.getRecipients());
        msg.setSubject(email.getSubject());
        msg.setText(email.getBody());

        try {
            javaMailSender.send(msg);
            LOG.info("Sent email to " + Arrays.toString(email.getRecipients()) + " with subject '"
                    + email.getSubject() + "'");
            return true;
        } catch (MailException ex) {
            LOG.error("Could not send email with subject: " + email.getSubject());
            throw ex;
        }
    }
}
