package org.patientview.importer.service.impl;

import org.apache.commons.lang.StringUtils;
import org.patientview.importer.service.EmailService;
import org.patientview.persistence.model.ContactPoint;
import org.patientview.persistence.model.Email;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.patientview.persistence.repository.GroupRepository;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/02/2015
 */
@Service
public class EmailServiceImpl extends AbstractServiceImpl<EmailServiceImpl> implements EmailService {

    @Inject
    private JavaMailSenderImpl javaMailSender;

    @Inject
    private GroupRepository groupRepository;
    
    @Inject
    private Properties properties;

    public void sendErrorEmail(String message, String identifier, String groupCode, boolean onlyToCentralSupport) {
        Group group = groupRepository.findByCode(groupCode);
        
        Email email = new Email();
        email.setSenderEmail(properties.getProperty("smtp.sender.email"));
        email.setSenderName(properties.getProperty("smtp.sender.name"));
        email.setSubject("PatientView - Importer Error");
        email.setRecipients(getRecipients(group, onlyToCentralSupport));
        
        StringBuilder sb = new StringBuilder();
        sb.append("Dear PatientView Support, <br/><br/>There has been an error importing patient data:<br/>");

        if (group == null && identifier == null) {
            sb.append("<br/>Could not convert incoming data.");
        }

        // if identifier is set, add identifier information
        if (StringUtils.isNotEmpty(identifier)) {
            sb.append("<br/>Identifier: ").append(identifier);
        }

        // add error message
        sb.append("<br/>Message: ").append(message);
        email.setBody(sb.toString());

        // try and send but ignore if exception and log
        try {
            sendEmail(email);
        } catch (MailException | MessagingException me) {
            LOG.error("Cannot send email: {}", me);
        }
    }

    private String[] getRecipients(Group group, boolean onlyToCentralSupport) {
        String centralSupportEmail = properties.getProperty("central.support.contact.email");

        // if only for central support, single email address
        if (onlyToCentralSupport) {
            return new String[]{centralSupportEmail};
        }

        List<String> recipientEmails = new ArrayList<>();

        // if group is set, get appropriate pv admin email address from group contact points
        if (group != null) {
            for (ContactPoint contactPoint : group.getContactPoints()) {
                if (contactPoint.getContactPointType().getValue().equals(ContactPointTypes.PV_ADMIN_EMAIL)) {
                    recipientEmails.add(contactPoint.getContent());
                }
            }
        }

        // if no PV admin emails, send to central support
        if (recipientEmails.isEmpty()) {
            recipientEmails.add(centralSupportEmail);
        }

        return recipientEmails.toArray(new String[recipientEmails.size()]);
    }
    
    private boolean sendEmail(Email email) throws MailException, MessagingException {
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
                if (email.isBcc()) {
                    helper.setBcc(email.getRecipients());
                } else {
                    helper.setTo(email.getRecipients());
                }
            }

            try {
                InternetAddress fromAddress = new InternetAddress(email.getSenderEmail(), email.getSenderName());
                helper.setFrom(fromAddress);

                if (email.isBcc()) {
                    fromAddress.setPersonal("PatientView User");
                    helper.setTo(fromAddress);
                }
            } catch (UnsupportedEncodingException uee) {
                helper.setFrom(email.getSenderEmail());

                if (email.isBcc()) {
                    helper.setTo(email.getSenderEmail());
                }
            }

            helper.setText(properties.getProperty("email.header") + email.getBody()
                    + properties.getProperty("email.footer"), true);

            try {
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
