package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.patientview.persistence.model.enums.ImmunisationCodelist;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Immunisation entity to store immunisation records for Patient
 */
@Entity
@Table(name = "pv_immunisation")
public class Immunisation extends AuditModel {

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "codelist", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImmunisationCodelist codelist;

    @Column(name = "other")
    private String other;

    @Column(name = "immunisation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date immunisationDate;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ImmunisationCodelist getCodelist() {
        return codelist;
    }

    public void setCodelist(ImmunisationCodelist codelist) {
        this.codelist = codelist;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public Date getImmunisationDate() {
        return immunisationDate;
    }

    public void setImmunisationDate(Date immunisationDate) {
        this.immunisationDate = immunisationDate;
    }
}
