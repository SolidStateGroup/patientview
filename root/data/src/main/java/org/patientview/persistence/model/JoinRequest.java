package org.patientview.persistence.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd-MM-yyyy")
    @Column(name = "date_of_birth")
    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    @Column(name = "nhs_number")
    private String nhsNumber;

    @OneToOne
    @JoinColumn(name = "specialty_id")
    private Group specialty;

    @OneToOne
    @JoinColumn(name  = "unit_id")
    private Group unit;

    @Column(name = "email")
    private String email;

    @Column(name = "creation_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

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

    public Group getSpecialty() {
        return specialty;
    }

    public void setSpecialty(final Group specialty) {
        this.specialty = specialty;
    }

    public Group getUnit() {
        return unit;
    }

    public void setUnit(final Group unit) {
        this.unit = unit;
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
}
