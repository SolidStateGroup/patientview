package org.patientview.api.service.impl;

import org.patientview.api.model.Email;
import org.patientview.api.service.EmailService;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Arrays;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
@Service
public class EmailServiceImpl extends AbstractServiceImpl<EmailServiceImpl> implements EmailService {

    @Inject
    private JavaMailSenderImpl javaMailSender;

    @Inject
    private Properties properties;

    public boolean sendEmail(Email email) throws MailException, MessagingException {

        // only send emails if enabled in properties file
        if (Boolean.parseBoolean(properties.getProperty("email.enabled"))) {

            // set HTML email content
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setSubject(email.getSubject());

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(email.getSender());
            helper.setText(email.getBody(), true);

            // if redirect enabled in properties the send to redirect email not actual recipient
            if (Boolean.parseBoolean(properties.getProperty("email.redirect.enabled"))) {
                helper.setTo(properties.getProperty("email.redirect.address").split(","));
            } else {
                helper.setTo(email.getRecipients());
            }

            try {
                javaMailSender.send(message);
                LOG.info("Sent email to " + Arrays.toString(email.getRecipients()) + " with subject '"
                        + email.getSubject() + "'");
                return true;
            } catch (MailException ex) {
                LOG.error("Could not send email with subject: " + email.getSubject());
                throw ex;
            }
        } else {
            return true;
        }
    }
}
