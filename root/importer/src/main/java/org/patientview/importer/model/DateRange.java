package org.patientview.importer.model;

import generated.Patientview;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/10/2014
 */
public class DateRange {

    private static final int LAST_HOUR = 23;
    private static final int LAST_MINUTE = 59;
    private static final int LAST_SECOND = 59;
    private static final int LAST_MILLISECOND = 999;

    private Date start;
    private Date end;

    public DateRange () { }

    public DateRange (Patientview.Patient.Testdetails.Test.Daterange daterange) {
        setStart(daterange.getStart().toGregorianCalendar().getTime());

        // to end of day
        Calendar stop = daterange.getStop().toGregorianCalendar();
        stop.set(Calendar.HOUR_OF_DAY, LAST_HOUR);
        stop.set(Calendar.MINUTE, LAST_MINUTE);
        stop.set(Calendar.SECOND, LAST_SECOND);
        stop.set(Calendar.MILLISECOND, LAST_MILLISECOND);

        setEnd(stop.getTime());
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }
}
