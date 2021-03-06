package org.patientview.persistence.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
public class Email {
    private String body;
    private String subject;
    private String senderEmail;
    private String senderName;
    private String[] recipients;
    private boolean bcc;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String[] getRecipients() {
        return recipients;
    }

    public void setRecipients(String[] recipients) {
        this.recipients = recipients;
    }

    public boolean isBcc() {
        return bcc;
    }

    public void setBcc(boolean bcc) {
        this.bcc = bcc;
    }
}
