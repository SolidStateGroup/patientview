package org.patientview.model;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 22/10/14
 */
public class LoginDetails {

    private String username;
    private String password;

    public LoginDetails () {

    }

    public LoginDetails(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
