package org.patientview;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by eatek on 16/05/2014.
 */
public class Log implements Serializable {

    private Date date;
    private String className;
    private String username;
    private String message;


    public Date getDate() {
        return date;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(final String className) {
        this.className = className;
    }
}
