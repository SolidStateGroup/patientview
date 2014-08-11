package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.StatisticPeriod;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
@Entity
@Table(name = "pv_group_statistic")
public class GroupStatistic extends BaseModel {

    @OneToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @OneToOne
    @JoinColumn(name = "type_id", nullable = false)
    private Lookup statisticType;

    @Column(name = "value")
    private BigInteger value;

    @Enumerated(EnumType.STRING)
    @Column(name = "Collated_Period")
    private StatisticPeriod statisticPeriod;

    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(final Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(final Date endDate) {
        this.endDate = endDate;
    }

    public Lookup getStatisticType() {
        return statisticType;
    }

    public void setStatisticType(final Lookup statisticType) {
        this.statisticType = statisticType;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(final BigInteger value) {
        this.value = value;
    }

    public StatisticPeriod getStatisticPeriod() {
        return statisticPeriod;
    }

    public void setStatisticPeriod(final StatisticPeriod statisticPeriod) {
        this.statisticPeriod = statisticPeriod;
    }
}
