package org.patientview.config.exception;

/**
 * Exception for use this the custom annotation exception
 *
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
public class ResourceForbiddenException extends Exception {

    public ResourceForbiddenException(String message) {
        super(message);
    }

}
