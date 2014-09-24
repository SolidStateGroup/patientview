package org.patientview.api.model;

import org.hl7.fhir.instance.model.DateAndTime;
import org.patientview.persistence.model.UserFeature;

import java.lang.Boolean;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class User extends BaseUser{

    private String email;
    private Set<UserFeature> userFeatures = new HashSet<>();
    private Set<GroupRole> groupRoles = new HashSet<>();
    private Date lastLogin;
    private Set identifiers;
    private Boolean locked;
    private Boolean emailVerified;
    private Boolean dummy;

    // FHIR
    private Date dateOfBirth;

    public User() {

    }

    public User(org.patientview.persistence.model.User user, org.hl7.fhir.instance.model.Patient patient) {
        setId(user.getId());
        setUsername(user.getUsername());
        setForename(user.getForename());
        setSurname(user.getSurname());
        setEmail(user.getEmail());
        setUserFeatures(user.getUserFeatures());
        setLastLogin(user.getLastLogin());
        setLocked(user.getLocked());
        setEmailVerified(user.getEmailVerified());
        setDummy(user.getDummy());

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

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getDummy() {
        return dummy;
    }

    public void setDummy(Boolean dummy) {
        this.dummy = dummy;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
