package org.patientview.api.model;

/**
 * GpDetails, used when validating and creating GP user accounts
 * Created by jamesr@solidstategroup.com
 * Created on 08/02/2016
 */
public class GpDetails {

    private String email;
    private String forename;
    private String patientIdentifier;
    private String signupKey;
    private String surname;

    public GpDetails() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getSignupKey() {
        return signupKey;
    }

    public void setSignupKey(String signupKey) {
        this.signupKey = signupKey;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
