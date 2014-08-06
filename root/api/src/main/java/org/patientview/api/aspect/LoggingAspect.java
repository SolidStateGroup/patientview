package org.patientview.api.aspect;

/**
 * Created by james@solidstategroup.com
 * Created on 29/07/2014
 */
//@Aspect
//@Configurable
public class LoggingAspect {

    private static LoggingAspect instance;

    private LoggingAspect() {

    }

    public static LoggingAspect aspectOf(){

        if (instance == null) {
            instance = new LoggingAspect();
        }
        return instance;
    }


}
