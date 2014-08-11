package org.patientview.api.timer.impl;

import org.patientview.api.timer.Timer;
import org.springframework.stereotype.Service;

import java.util.Calendar;

/**
 * Created by james@solidstategroup.com
 * Created on 11/08/2014
 */
@Service
public class TimerImpl implements Timer {

    @Override
    public Calendar getCurrentDate() {
        return Calendar.getInstance();
    }
}
