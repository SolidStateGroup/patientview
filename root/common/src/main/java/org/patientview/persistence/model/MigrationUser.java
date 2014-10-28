package org.patientview.persistence.model;

/**
 * Transport object used for user migration
 *
 * Created by jamesr@solidstategroup.com
 * Created on 24/10/2014
 */
public class MigrationUser {

    // User
    private User user;

    public MigrationUser () {

    }

    public MigrationUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
