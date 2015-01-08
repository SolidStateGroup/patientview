package org.patientview.api.service;

import org.patientview.persistence.model.Email;
import org.springframework.mail.MailException;

import javax.mail.MessagingException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
public interface EmailService {
    boolean sendEmail(Email email) throws MailException, MessagingException;
}
