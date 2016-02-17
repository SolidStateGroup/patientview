package org.patientview.api.model;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.enums.PatientMessagingFeatureType;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * UserToken, representing a large amount of User and static information, retrieved after a User has authenticated
 * successfully and has retrieved their authentication token.
 * Minimal version returned when User has a secret word set for further multi-factor authentication.
 * Created by jamesr@solidstategroup.com
 * Created on 25/09/2014
 */
public class UserToken {

    private User user;
    private String token;
    private Date expiration;
    private Date created;

    // set separately after authentication
    private List<Role> securityRoles;
    private List<Feature> userFeatures;
    private List<BaseGroup> userGroups;
    private Set<Route> routes;
    private List<Role> staffRoles;
    private List<Role> patientRoles;
    private List<Feature> groupFeatures;
    private List<Feature> staffFeatures;
    private List<Feature> patientFeatures;
    private List<PatientMessagingFeatureType> patientMessagingFeatureTypes;
    private List<String> auditActions;
    private boolean groupMessagingEnabled;

    // used with multi factor authentication
    private List<String> secretWordIndexes;
    private Map<String, String> secretWordChoices;
    private boolean checkSecretWord;

    public UserToken() {
    }

    public UserToken(String token) {
        setToken(token);
    }

    public UserToken(org.patientview.persistence.model.UserToken userToken) {
        if (userToken.getUser() != null) {
            setUser(new User(userToken.getUser()));
        }
        setToken(userToken.getToken());
        setExpiration(userToken.getExpiration());
        setCreated(userToken.getCreated());
        setCheckSecretWord(userToken.isCheckSecretWord());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<Role> getSecurityRoles() {
        return securityRoles;
    }

    public void setSecurityRoles(List<Role> securityRoles) {
        this.securityRoles = securityRoles;
    }

    public List<Feature> getUserFeatures() {
        return userFeatures;
    }

    public void setUserFeatures(List<Feature> userFeatures) {
        this.userFeatures = userFeatures;
    }

    public List<BaseGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<BaseGroup> userGroups) {
        this.userGroups = userGroups;
    }

    public Set<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(Set<Route> routes) {
        this.routes = routes;
    }

    public List<Role> getStaffRoles() {
        return staffRoles;
    }

    public void setStaffRoles(List<Role> staffRoles) {
        this.staffRoles = staffRoles;
    }

    public List<Role> getPatientRoles() {
        return patientRoles;
    }

    public void setPatientRoles(List<Role> patientRoles) {
        this.patientRoles = patientRoles;
    }

    public List<Feature> getGroupFeatures() {
        return groupFeatures;
    }

    public void setGroupFeatures(List<Feature> groupFeatures) {
        this.groupFeatures = groupFeatures;
    }

    public List<Feature> getPatientFeatures() {
        return patientFeatures;
    }

    public void setPatientFeatures(List<Feature> patientFeatures) {
        this.patientFeatures = patientFeatures;
    }

    public List<Feature> getStaffFeatures() {
        return staffFeatures;
    }

    public void setStaffFeatures(List<Feature> staffFeatures) {
        this.staffFeatures = staffFeatures;
    }

    public List<PatientMessagingFeatureType> getPatientMessagingFeatureTypes() {
        return patientMessagingFeatureTypes;
    }

    public void setPatientMessagingFeatureTypes(List<PatientMessagingFeatureType> patientMessagingFeatureTypes) {
        this.patientMessagingFeatureTypes = patientMessagingFeatureTypes;
    }

    public List<String> getAuditActions() {
        return auditActions;
    }

    public void setAuditActions(List<String> auditActions) {
        this.auditActions = auditActions;
    }

    public boolean isGroupMessagingEnabled() {
        return groupMessagingEnabled;
    }

    public void setGroupMessagingEnabled(boolean groupMessagingEnabled) {
        this.groupMessagingEnabled = groupMessagingEnabled;
    }

    public List<String> getSecretWordIndexes() {
        return secretWordIndexes;
    }

    public void setSecretWordIndexes(List<String> secretWordIndexes) {
        this.secretWordIndexes = secretWordIndexes;
    }

    public Map<String, String> getSecretWordChoices() {
        return secretWordChoices;
    }

    public void setSecretWordChoices(Map<String, String> secretWordChoices) {
        this.secretWordChoices = secretWordChoices;
    }

    public boolean isCheckSecretWord() {
        return checkSecretWord;
    }

    public void setCheckSecretWord(boolean checkSecretWord) {
        this.checkSecretWord = checkSecretWord;
    }
}
