package org.patientview.config.exception;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 03/11/2014
 */
public class MigrationException extends Exception {

    public MigrationException(final Exception e) {
        super(e);
    }

    public MigrationException(final String message) {
        super(message);
    }

    public MigrationException(final String message, final Exception exception) {
        super(message, exception);
    }
}

