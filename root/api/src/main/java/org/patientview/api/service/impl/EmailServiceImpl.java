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

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
@Service
public class EmailServiceImpl extends AbstractServiceImpl<EmailServiceImpl> implements EmailService {

    @Inject
    private JavaMailSenderImpl javaMailSender;

    public boolean sendEmail(Email email) throws MailException, MessagingException {

        // set HTML email content
        MimeMessage message = javaMailSender.createMimeMessage();
        message.setSubject(email.getSubject());

        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(email.getSender());
        helper.setTo(email.getRecipients());
        helper.setText(email.getBody(), true);

        try {
            javaMailSender.send(message);
            LOG.info("Sent email to " + Arrays.toString(email.getRecipients()) + " with subject '"
                    + email.getSubject() + "'");
            return true;
        } catch (MailException ex) {
            LOG.error("Could not send email with subject: " + email.getSubject());
            throw ex;
        }
    }
}
