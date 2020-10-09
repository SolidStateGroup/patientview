package org.patientview.api.service.impl;

import org.patientview.api.service.EmailService;
import org.patientview.persistence.model.Email;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com Created on 24/06/2014
 */
@Service
public class EmailServiceImpl extends AbstractServiceImpl<EmailServiceImpl> implements EmailService {

    private static final String IGNORE_EMAIL_DOMAIN = "@patientview.org";

    @Inject
    private JavaMailSenderImpl javaMailSender;

    @Inject
    private Properties properties;

    private static final int BATCH_SIZE = 50;

    /**
     * @inheritDoc
     */
    @Override
    public boolean sendEmail(Email email) throws MailException, MessagingException {
        // only send emails if enabled in properties file
        if (Boolean.parseBoolean(properties.getProperty("email.enabled"))) {

            // send emails in batches of 50 to avoid SMTP send error due to too many participants
            List<String[]> recipientBatches = splitArray(email.getRecipients(), BATCH_SIZE);

            // set HTML email content
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setSubject(email.getSubject());
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            for (String[] recipientBatch : recipientBatches) {
                if (recipientBatch.length > 0) {
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
                                helper.setBcc(recipientBatch);
                            } else {
                                helper.setTo(recipientBatch);
                            }
                        } catch (AddressException ae) {
                            LOG.error("Email: Address Exception, could not send email to "
                                    + Arrays.toString(recipientBatch) + " with subject '"
                                    + email.getSubject() + "'");
                            throw ae;
                        }
                    }

                    try {
                        InternetAddress fromAddress
                                = new InternetAddress(email.getSenderEmail(), email.getSenderName());
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
                        LOG.info("Email: Sent email to " + Arrays.toString(recipientBatch) + " with subject '"
                                + email.getSubject() + "'");
                    } catch (MailException ex) {
                        LOG.error("Email: Could not send email to " + Arrays.toString(recipientBatch)
                                + " with subject '" + email.getSubject() + "'");
                        throw ex;
                    }
                }
            }
            return true;
        } else {
            LOG.info("Email: Email sending not enabled");
            return true;
        }
    }

    /**
     * Validate email address. Must be a valid email address and does not contain
     *
     * @param email String email address to validate
     * @return false if not a valid email address
     */
    private boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            if (email.contains(IGNORE_EMAIL_DOMAIN)) {
                LOG.info("Dummy email address " + email + ", will ignore");
                return false;
            }

            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    /**
     * Split an array of Strings into a List of smaller arrays.
     *
     * @param originalArray String array
     * @param chunkSize     int size of new smaller arrays
     * @return List of String arrays
     */
    private List<String[]> splitArray(String[] originalArray, int chunkSize) {
        List<String> validEmails = new ArrayList<>();
        for (String email : originalArray) {
            if (isValidEmailAddress(email)) {
                validEmails.add(email);
            }
        }

        String[] validArray = validEmails.toArray(new String[validEmails.size()]);

        List<String[]> listOfArrays = new ArrayList<>();
        int totalSize = validArray.length;
        if (totalSize < chunkSize) {
            chunkSize = totalSize;
        }
        int from = 0;
        int to = chunkSize;

        while (from < totalSize) {
            String[] partArray = Arrays.copyOfRange(validArray, from, to);
            listOfArrays.add(partArray);

            from += chunkSize;
            to = from + chunkSize;
            if (to > totalSize) {
                to = totalSize;
            }
        }
        return listOfArrays;
    }
}
