package org.patientview.api.model;

import org.patientview.persistence.model.Identifier;

/**
 * UserIdentifier, representing the link between a User and one of their Identifiers, used when validating identifiers.
 * Created by jamesr@solidstategroup.com
 * Created on 23/10/2014
 */
public class UserIdentifier {

    private Long userId;
    private boolean dummy;
    private Identifier identifier;

    public UserIdentifier() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isDummy() {
        return dummy;
    }

    public void setDummy(boolean dummy) {
        this.dummy = dummy;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }
}
