package org.patientview.config.exception;

/**
 * Created by jamest@solidstategroup.com
 * Created on 06/01/2015
 */
public class UktException extends Exception {

    public UktException(final Exception e) {
        super(e);
    }

    public UktException(final String message) {
        super(message);
    }

    public UktException(final String message, final Exception exception) {
        super(message, exception);
    }
}

