package org.patientview.api.model;

import org.patientview.persistence.model.enums.JoinRequestStatus;

import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 23/09/2014
 */
public class JoinRequest {

    private Long id;
    private String forename;
    private String surname;
    private Date dateOfBirth;
    private String nhsNumber;
    private Group group;
    private String email;
    private Date created;
    private JoinRequestStatus status;
    private Date completionDate;
    private User completedBy;
    private String notes;

    public JoinRequest() {

    }

    public JoinRequest(org.patientview.persistence.model.JoinRequest joinRequest) {
        setId(joinRequest.getId());
        setForename(joinRequest.getForename());
        setSurname(joinRequest.getSurname());
        setDateOfBirth(joinRequest.getDateOfBirth());
        setNhsNumber(joinRequest.getNhsNumber());

        if (joinRequest.getGroup() != null) {
            setGroup(new Group(joinRequest.getGroup()));
        }

        setEmail(joinRequest.getEmail());
        setCreated(joinRequest.getCreated());
        setStatus(joinRequest.getStatus());
        setCompletionDate(joinRequest.getCompletionDate());

        if (joinRequest.getCompletedBy() != null) {
            setCompletedBy(new User(joinRequest.getCompletedBy(), null));
        }

        setNotes(joinRequest.getNotes());

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(String nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public JoinRequestStatus getStatus() {
        return status;
    }

    public void setStatus(JoinRequestStatus status) {
        this.status = status;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public User getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(User completedBy) {
        this.completedBy = completedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
