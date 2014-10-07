package org.patientview.api.model;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Route;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
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
    private List<BaseGroup> userGroups;
    private Set<Route> routes;
    private List<Role> staffRoles;
    private List<Role> patientRoles;
    private List<Feature> groupFeatures;
    private List<Feature> staffFeatures;
    private List<Feature> patientFeatures;

    public UserToken () {

    }

    public UserToken (org.patientview.persistence.model.UserToken userToken) {
        if (userToken.getUser() != null) {
            setUser(new User(userToken.getUser(), null));
        }
        setToken(userToken.getToken());
        setExpiration(userToken.getExpiration());
        setCreated(userToken.getCreated());
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
}
