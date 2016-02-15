package org.patientview.api.model;

import org.patientview.persistence.model.GpPatient;

import java.util.ArrayList;
import java.util.List;

/**
 * GpDetails, used when validating and creating GP user accounts
 * Created by jamesr@solidstategroup.com
 * Created on 08/02/2016
 */
public class GpDetails {

    private String email;
    private String forename;
    private String password;
    private String patientIdentifier;
    private String signupKey;
    private String surname;
    private String username;

    // reduced information on practices associated with GP, found from GP master table
    private List<GpPractice> practices = new ArrayList<>();

    // reduced information on users, with gp name from FHIR
    private List<GpPatient> patients = new ArrayList<>();

    // used in step 2 ui
    private String centralSupportEmail;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<GpPractice> getPractices() {
        return practices;
    }

    public void setPractices(List<GpPractice> practices) {
        this.practices = practices;
    }

    public List<GpPatient> getPatients() {
        return patients;
    }

    public void setPatients(List<GpPatient> patients) {
        this.patients = patients;
    }

    public String getCentralSupportEmail() {
        return centralSupportEmail;
    }

    public void setCentralSupportEmail(String centralSupportEmail) {
        this.centralSupportEmail = centralSupportEmail;
    }
}
