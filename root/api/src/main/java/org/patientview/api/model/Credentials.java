package org.patientview.api.model;

/**
 * Credentials, representing a simple username, password object used when resetting passwords.
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
public class Credentials {

    private String username;
    private String password;

    // only used for importer
    private String apiKey;

    public Credentials() { }

    public Credentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Credentials(String username, String password, String apiKey) {
        this.username = username;
        this.password = password;
        this.apiKey = apiKey;
    }

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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
