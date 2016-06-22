package org.patientview.config.exception;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public class ImportResourceException extends Exception {
    boolean anonymous = false;

    public ImportResourceException(String message) {
        super(message);
    }

    public ImportResourceException(String message, boolean anonymous) {
        super(message);
        this.anonymous = anonymous;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }
}
