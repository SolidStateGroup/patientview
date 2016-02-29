package org.patientview.api.model;

import org.patientview.persistence.model.UserFeature;

/**
 * ExternalConversation used to handle creation of conversations with users by external systems
 */
public class ExternalConversation {

    private String groupCode;
    private String identifier;
    private String message;
    private String sender;
    private String title;
    private String token;
    private UserFeature userFeature;
    private String username;

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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}