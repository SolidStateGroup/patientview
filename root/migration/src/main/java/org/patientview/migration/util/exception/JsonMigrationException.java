package org.patientview.migration.util.exception;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
public class JsonMigrationException extends Exception {
    public JsonMigrationException(Throwable throwable) {
        super(throwable);
    }

    public JsonMigrationException(String message) {
        super(message);
    }
}
