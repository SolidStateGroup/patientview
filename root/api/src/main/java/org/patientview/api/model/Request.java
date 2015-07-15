package org.patientview.api.model;

import org.patientview.persistence.model.enums.RequestStatus;
import org.patientview.persistence.model.enums.RequestTypes;

import java.util.Date;

/**
 * Request, representing the information required when a potential patient is requesting to join PatientView.
 * Created by james@solidstategroup.com
 * Created on 23/09/2014
 */
public class Request {

    private Long id;
    private String forename;
    private String surname;
    private Date dateOfBirth;
    private String nhsNumber;
    private Group group;
    private String email;
    private Date created;
    private RequestStatus status;
    private Date completionDate;
    private User completedBy;
    private String notes;
    private String captcha;
    private RequestTypes type;

    public Request() {
    }

    public Request(org.patientview.persistence.model.Request request) {
        setId(request.getId());
        setForename(request.getForename());
        setSurname(request.getSurname());
        setDateOfBirth(request.getDateOfBirth());
        setNhsNumber(request.getNhsNumber());

        if (request.getGroup() != null) {
            setGroup(new Group(request.getGroup()));
        }

        setEmail(request.getEmail());
        setCreated(request.getCreated());
        setStatus(request.getStatus());
        setCompletionDate(request.getCompletionDate());

        if (request.getCompletedBy() != null) {
            setCompletedBy(new User(request.getCompletedBy()));
        }

        setNotes(request.getNotes());
        setType(request.getType());
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

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
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

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public RequestTypes getType() {
        return type;
    }

    public void setType(RequestTypes type) {
        this.type = type;
    }
}
