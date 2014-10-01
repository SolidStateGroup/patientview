package org.patientview.api.model;

import java.util.List;
import java.util.Map;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 01/09/2014
 */
public class UserResultCluster {

    private Long day;
    private Long month;
    private Long year;
    private Long hour;
    private Long minute;
    private List<IdValue> values;

    public Long getDay() {
        return day;
    }

    public void setDay(Long day) {
        this.day = day;
    }

    public Long getMonth() {
        return month;
    }

    public void setMonth(Long month) {
        this.month = month;
    }

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    public Long getHour() {
        return hour;
    }

    public void setHour(Long hour) {
        this.hour = hour;
    }

    public Long getMinute() {
        return minute;
    }

    public void setMinute(Long minute) {
        this.minute = minute;
    }

    public List<IdValue> getValues() {
        return values;
    }

    public void setValues(List<IdValue> values) {
        this.values = values;
    }
}
