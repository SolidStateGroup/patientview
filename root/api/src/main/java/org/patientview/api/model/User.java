package org.patientview.api.model;

import org.hl7.fhir.instance.model.*;
import org.patientview.persistence.model.BaseModel;
import org.patientview.persistence.model.UserFeature;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class User extends BaseModel{

    private String forename;
    private String surname;
    private String email;
    private Set<UserFeature> userFeatures = new HashSet<>();
    private Set<GroupRole> groupRoles = new HashSet<>();
    private Date lastLogin;
    private Set identifiers;

    // FHIR
    private Date dateOfBirth;

    public User() {
    }

    public User(org.patientview.persistence.model.User user, org.hl7.fhir.instance.model.Patient patient) {
        setId(user.getId());
        setForename(user.getForename());
        setSurname(user.getSurname());
        setEmail(user.getEmail());
        setUserFeatures(user.getUserFeatures());
        setLastLogin(user.getLastLogin());

        for (org.patientview.persistence.model.GroupRole groupRole : user.getGroupRoles()) {
            getGroupRoles().add(new GroupRole(groupRole));
        }

        // if user has fhirPatient data (is a patient)
        if (patient != null) {
            // set date of birth
            if (patient.getBirthDateSimple() != null) {
                DateAndTime fhirDateOfBirth = patient.getBirthDateSimple();
                    setDateOfBirth(new Date(new GregorianCalendar(fhirDateOfBirth.getYear(),
                        fhirDateOfBirth.getMonth()-1, fhirDateOfBirth.getDay()).getTimeInMillis()));
            }
        }

        setIdentifiers(user.getIdentifiers());
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<UserFeature> getUserFeatures() {
        return userFeatures;
    }

    public void setUserFeatures(Set<UserFeature> userFeatures) {
        this.userFeatures = userFeatures;
    }

    public Set<GroupRole> getGroupRoles() {
        return groupRoles;
    }

    public void setGroupRoles(Set<GroupRole> groupRoles) {
        this.groupRoles = groupRoles;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Set getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set identifiers) {
        this.identifiers = identifiers;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
