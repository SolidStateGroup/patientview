package org.patientview.api.model;

/**
 * Created by james@solidstategroup.com
 * Created on 19/08/2014
 */
public class ForgottenCredentials {

    private String username;
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
