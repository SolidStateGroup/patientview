package org.patientview.config.exception;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 19/11/2014
 */
public class VerificationException extends Exception {

    public VerificationException(final Exception e) {
        super(e);
    }

    public VerificationException(final String message) {
        super(message);
    }

    public VerificationException(final String message, final Exception exception) {
        super(message, exception);
    }
}

