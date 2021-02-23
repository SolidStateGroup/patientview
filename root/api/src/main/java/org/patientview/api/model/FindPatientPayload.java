package org.patientview.api.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Payload used to find patient.
 *
 * Created by Pavlo Maksymchuk.
 */
public class FindPatientPayload implements Serializable {

    private String searchUsername;
    private String searchIdentifier;
    private String searchEmail;
    private Date dateOfBirth;

    public FindPatientPayload() {
    }

    public String getSearchUsername() {
        return searchUsername;
    }

    public void setSearchUsername(String searchUsername) {
        this.searchUsername = searchUsername;
    }

    public String getSearchIdentifier() {
        return searchIdentifier;
    }

    public void setSearchIdentifier(String searchIdentifier) {
        this.searchIdentifier = searchIdentifier;
    }

    public String getSearchEmail() {
        return searchEmail;
    }

    public void setSearchEmail(String searchEmail) {
        this.searchEmail = searchEmail;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
