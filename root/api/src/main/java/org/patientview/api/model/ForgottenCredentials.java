package org.patientview.api.model;

/**
 * ForgottenCredentials, used when a User has forgotten their password but can provide their username and email.
 * Created by james@solidstategroup.com
 * Created on 19/08/2014
 */
public class ForgottenCredentials {

    private String username;
    private String email;
    private String captcha;

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

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
