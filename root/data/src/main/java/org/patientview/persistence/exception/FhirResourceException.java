package org.patientview.persistence.exception;

/**
 * Created by james@solidstategroup.com
 * Created on 26/08/2014
 */
public class FhirResourceException extends Exception {

    public FhirResourceException(final Exception e) {
        super(e);
    }

    public FhirResourceException(final String message) {
        super(message);
    }

    public FhirResourceException(final String message, final Exception exception) {
        super(message, exception);
    }
}

