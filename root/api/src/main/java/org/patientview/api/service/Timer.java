package org.patientview.api.service;

import java.util.Calendar;

/**
 * Abstraction of the getDate functionality.
 *
 * Created by james@solidstategroup.com
 * Created on 11/08/2014
 */
public interface Timer {

    /**
     * Get an instance of Calendar.
     * @return Calendar object
     */
    Calendar getCurrentDate();
}
