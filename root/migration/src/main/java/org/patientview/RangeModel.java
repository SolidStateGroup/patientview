package org.patientview;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@MappedSuperclass
public class RangeModel extends AuditModel {

    @Column(name = "Start_Date")
    @Temporal(TemporalType.DATE)
    private Date startDate = new Date();

    @Column(name = "End_Date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    protected RangeModel() {
    }

    @JsonIgnore
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    @JsonIgnore
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

}
