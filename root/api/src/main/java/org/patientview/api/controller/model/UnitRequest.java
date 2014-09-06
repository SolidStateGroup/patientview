package org.patientview.api.controller.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.patientview.persistence.model.Group;

import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 19/08/2014
 */
public class UnitRequest {

    private String forename;
    private String surname;
    private Date dateOfBirth;
    private String nhsNumber;
    private Group group;
    private String email;

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
}
