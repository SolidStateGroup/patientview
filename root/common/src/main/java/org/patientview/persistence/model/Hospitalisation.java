package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Hospitalisation entity to store hospitalisation records for Patient
 */
@Entity
@Table(name = "pv_hospitalisation")
public class Hospitalisation extends AuditModel {

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date_admitted", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateAdmitted;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "date_discharged")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDischarged;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDateAdmitted() {
        return dateAdmitted;
    }

    public void setDateAdmitted(Date dateAdmitted) {
        this.dateAdmitted = dateAdmitted;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getDateDischarged() {
        return dateDischarged;
    }

    public void setDateDischarged(Date dateDischarged) {
        this.dateDischarged = dateDischarged;
    }
}
