package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.ConversationTypes;
import org.patientview.persistence.model.enums.FeatureType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/08/2014
 */
@Entity
@Table(name = "pv_conversation")
public class Conversation extends AuditModel {

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private ConversationTypes type;

    @Column(name = "image_data")
    private String imageData;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "status")
    private Integer status;

    @Column(name = "open")
    private Boolean open;

    @Column(name = "title")
    private String title;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ConversationUser> conversationUsers;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
    @OrderBy("created ASC")
    private Set<Message> messages;

    // used when contacting unit, staff with staff feature typically UNIT_TECHNICAL_CONTACT or PATIENT_SUPPORT_CONTACT
    @Transient
    @Enumerated(EnumType.STRING)
    private FeatureType staffFeature;

    // used when contacting unit, id of the group to contact
    @Transient
    private Long groupId;

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

    public Set<Message> getMessages() {
        return messages;
    }

    public void setMessages(Set<Message> messages) {
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
