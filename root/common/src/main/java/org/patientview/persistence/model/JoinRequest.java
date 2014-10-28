package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.JoinRequestStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 30/07/2014
 */
@Entity
@Table(name = "pv_join_request")
public class JoinRequest extends BaseModel {

    @Column(name = "forename")
    private String forename;

    @Column(name = "surname")
    private String surname;

    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "nhs_number")
    private String nhsNumber;

    @OneToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(name = "email")
    private String email;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private JoinRequestStatus status;

    @Column(name = "completion_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completionDate;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "completed_by", nullable = true)
    private User completedBy;

    @Column(name = "notes")
    private String notes;

    // used when public creation of join requests
    @Transient
    private Long groupId;

    public String getForename() {
        return forename;
    }

    public void setForename(final String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(final String surname) {
        this.surname = surname;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNhsNumber() {
        return nhsNumber;
    }

    public void setNhsNumber(final String nhsNumber) {
        this.nhsNumber = nhsNumber;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(final Date created) {
        this.created = created;
    }

    public JoinRequestStatus getStatus() {
        return status;
    }

    public void setStatus(final JoinRequestStatus status) {
        this.status = status;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(final Date completionDate) {
        this.completionDate = completionDate;
    }

    public User getCompletedBy() {
        return completedBy;
    }

    public void setCompletedBy(final User completedBy) {
        this.completedBy = completedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(final String notes) {
        this.notes = notes;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
