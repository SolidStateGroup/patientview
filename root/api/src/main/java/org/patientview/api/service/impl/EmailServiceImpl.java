package org.patientview.api.service.impl;

import org.patientview.persistence.model.Email;
import org.patientview.api.service.EmailService;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
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

        //LOG.info("Email: Preparing to send email");

        // only send emails if enabled in properties file
        if (Boolean.parseBoolean(properties.getProperty("email.enabled"))) {

            // set HTML email content
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setSubject(email.getSubject());

            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // if redirect enabled in properties the send to redirect email not actual recipient
            if (Boolean.parseBoolean(properties.getProperty("email.redirect.enabled"))) {
                LOG.info("Email: Email redirect enabled");
                if (email.isBcc()) {
                    helper.setBcc(properties.getProperty("email.redirect.address").split(","));
                } else {
                    helper.setTo(properties.getProperty("email.redirect.address").split(","));
                }
            } else {
                try {
                    if (email.isBcc()) {
                        helper.setBcc(email.getRecipients());
                    } else {
                        helper.setTo(email.getRecipients());
                    }
                } catch (AddressException ae) {
                    LOG.error("Email: Address Exception, could not send email to "
                            + Arrays.toString(email.getRecipients()) + " with subject '"
                            + email.getSubject() + "'");
                    throw ae;
                }
            }

            try {
                InternetAddress fromAddress = new InternetAddress(email.getSenderEmail(), email.getSenderName());
                helper.setFrom(fromAddress);

                if (email.isBcc()) {
                    fromAddress.setPersonal("PatientView User");
                    helper.setTo(fromAddress);
                }
                //LOG.info("Email: Set from " + fromAddress.getPersonal() + " (" + fromAddress.getAddress() + ")");
            } catch (UnsupportedEncodingException uee) {
                helper.setFrom(email.getSenderEmail());

                if (email.isBcc()) {
                    helper.setTo(email.getSenderEmail());
                }
                //LOG.info("Email: Set from " + email.getSenderEmail());
            }

            helper.setText(properties.getProperty("email.header") + email.getBody()
                    + properties.getProperty("email.footer"), true);

            try {
                //LOG.info("Email: Attempting to send email to " + Arrays.toString(email.getRecipients())
                //        + " with subject '" + email.getSubject() + "'");
                javaMailSender.send(message);
                LOG.info("Email: Sent email to " + Arrays.toString(email.getRecipients()) + " with subject '"
                        + email.getSubject() + "'");
                return true;
            } catch (MailException ex) {
                LOG.error("Email: Could not send email to " + Arrays.toString(email.getRecipients()) + " with subject '"
                        + email.getSubject() + "'");
                throw ex;
            }
        } else {
            LOG.info("Email: Email sending not enabled");
            return true;
        }
    }
}
