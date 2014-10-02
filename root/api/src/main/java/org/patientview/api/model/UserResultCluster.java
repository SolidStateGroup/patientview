package org.patientview.api.model;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 01/09/2014
 */
public class UserResultCluster {

    private String day;
    private String month;
    private String year;
    private String hour;
    private String minute;
    private List<IdValue> values;
    private String comment;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getMinute() {
        return minute;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public List<IdValue> getValues() {
        return values;
    }

    public void setValues(List<IdValue> values) {
        this.values = values;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
