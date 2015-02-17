package org.patientview.api.model;

/**
 * Credentials, representing a simple username, password object used when resetting passwords.
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
public class Credentials {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }
}
