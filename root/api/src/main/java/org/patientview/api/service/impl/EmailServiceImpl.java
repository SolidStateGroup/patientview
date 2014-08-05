package org.patientview.api.service.impl;

import org.patientview.api.controller.model.Email;
import org.patientview.api.service.EmailService;
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

    @Inject
    private JavaMailSenderImpl javaMailSender;

    public boolean sendEmail(Email email) {
        // set email content
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(email.getSender());
        msg.setTo(email.getRecipients());
        msg.setSubject(email.getSubject());
        msg.setText(email.getBody());

        // send
        try{
            javaMailSender.send(msg);
            return true;
        }
        catch (MailException ex) {
            // todo: temporarily return true even if email failed
            // (requires git submodule setup for smtp.password from external file
            return true;
        }
    }
}
