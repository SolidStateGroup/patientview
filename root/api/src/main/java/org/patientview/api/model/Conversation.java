package org.patientview.api.model;

import org.patientview.persistence.model.enums.ConversationTypes;
import org.patientview.persistence.model.enums.FeatureType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Conversation, representing a conversation between a number of Users, containing multiple Messages.
 * Created by jamesr@solidstategroup.com
 * Created on 24/09/2014
 */
public class Conversation {

    private Long id;
    private ConversationTypes type;
    private String imageData;
    private Integer rating;
    private Integer status;
    private Boolean open;
    private String title;
    private Set<ConversationUser> conversationUsers;
    private List<Message> messages;
    private FeatureType staffFeature;
    private Long groupId;

    public Conversation() {
    }

    public Conversation(org.patientview.persistence.model.Conversation conversation) {
        setId(conversation.getId());
        setType(conversation.getType());
        setImageData(conversation.getImageData());
        setRating(conversation.getRating());
        setStatus(conversation.getStatus());
        setOpen(conversation.getOpen());
        setTitle(conversation.getTitle());

        setConversationUsers(new HashSet<ConversationUser>());

        if (conversation.getConversationUsers() != null) {
            for (org.patientview.persistence.model.ConversationUser conversationUser
                : conversation.getConversationUsers()) {
                getConversationUsers().add(new ConversationUser(conversationUser));
            }
        }

        setMessages(new ArrayList<Message>());

        if (conversation.getMessages() != null) {
            for (org.patientview.persistence.model.Message message : conversation.getMessages()) {
                getMessages().add(new Message(message));
            }
        }

        setStaffFeature(conversation.getStaffFeature());
        setGroupId(conversation.getGroupId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConversationTypes getType() {
        return type;
    }

    public void setType(ConversationTypes type) {
        this.type = type;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getOpen() {
        return open;
    }

    public void setOpen(Boolean open) {
        this.open = open;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<ConversationUser> getConversationUsers() {
        return conversationUsers;
    }

    public void setConversationUsers(Set<ConversationUser> conversationUsers) {
        this.conversationUsers = conversationUsers;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public FeatureType getStaffFeature() {
        return staffFeature;
    }

    public void setStaffFeature(FeatureType staffFeature) {
        this.staffFeature = staffFeature;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
