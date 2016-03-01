package org.patientview.api.model;

import org.patientview.persistence.model.UserFeature;

/**
 * ExternalConversation used to handle creation of conversations with users by external systems
 */
public class ExternalConversation {

    private String groupCode;
    private String identifier;
    private String message;
    private String recipientUsername;
    private String senderSystem;
    private String senderUsername;
    private String title;
    private String token;
    private UserFeature userFeature;

    // for returning success and error status
    private String errorMessage;
    private boolean success;
    private String successMessage;

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

    public UserFeature getUserFeature() {
        return userFeature;
    }

    public void setUserFeature(UserFeature userFeature) {
        this.userFeature = userFeature;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }
}