package org.patientview.api.model;

import org.patientview.persistence.model.Identifier;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 23/10/2014
 *
 * Used when validating identifiers on front end
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
