package org.patientview.api.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 27/08/2014
 */
public class BaseUser {

    private Long id;
    private String username;
    private String forename;
    private String surname;

    public BaseUser() {

    }

    public BaseUser(org.patientview.persistence.model.User user) {
        setId(user.getId());
        setUsername(user.getUsername());
        setForename(user.getForename());
        setSurname(user.getSurname());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
}
