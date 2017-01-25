package org.patientview.api.service;

import org.patientview.persistence.model.Email;
import org.springframework.mail.MailException;

import javax.mail.MessagingException;

/**
 * Email service, used to send Emails that are built in other services.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
public interface EmailService {

    /**
     * Send an Email, created in another service.
     * @param email Email object containing all required properties
     * @return True if successfully sent or mail redirected etc, false or exception if not
     * @throws MailException
     * @throws MessagingException
     */
    boolean sendEmail(Email email) throws MailException, MessagingException;

    /**
     * Send an Email, created in another service.
     * @param email Email object containing all required properties
     * @param donorview if email sent to donor user, will use different branding
     * @return True if successfully sent or mail redirected etc, false or exception if not
     * @throws MailException
     * @throws MessagingException
     */
    boolean sendEmail(Email email, boolean donorview) throws MailException, MessagingException;
}
