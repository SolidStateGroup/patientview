package org.patientview.api.exception;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 27/06/2014
 */
public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
