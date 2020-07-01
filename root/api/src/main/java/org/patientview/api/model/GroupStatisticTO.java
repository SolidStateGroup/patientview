package org.patientview.api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.patientview.persistence.model.enums.StatisticType;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

/**
 * GroupStatisticTO, representing a number of different Group statistics for a certain time range.
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
public class GroupStatisticTO implements Comparable {

    @JsonFormat(pattern="yyyy-MM-dd")
    private Date startDate;
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date endDate;

    private Map<StatisticType, BigInteger> statistics;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Map<StatisticType, BigInteger> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<StatisticType, BigInteger> statistics) {
        this.statistics = statistics;
    }

    public int compareTo(Object o) {
        if (o != null && o instanceof GroupStatisticTO) {
            GroupStatisticTO groupStatisticTO = (GroupStatisticTO) o;
            if (this.getStartDate().getTime() < groupStatisticTO.getStartDate().getTime()) {
                return -1;
            } else if (this.getStartDate().getTime() > groupStatisticTO.getStartDate().getTime()) {
                return 1;
            } else {
                return 0;
            }
        }
        return -1;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
