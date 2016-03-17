package org.patientview.api.model;

import org.patientview.persistence.model.ServerResponse;

/**
 * ExternalConversation used to handle creation of conversations with users by external systems
 */
public class ExternalConversation extends ServerResponse {

    private String groupCode;
    private String identifier;
    private String message;
    private String recipientUsername;
    private String senderSystem;
    private String senderUsername;
    private String title;
    private String token;
    private String userFeature;

    public ExternalConversation() { }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public String getSenderSystem() {
        return senderSystem;
    }

    public void setSenderSystem(String senderSystem) {
        this.senderSystem = senderSystem;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserFeature() {
        return userFeature;
    }

    public void setUserFeature(String userFeature) {
        this.userFeature = userFeature;
    }
}
