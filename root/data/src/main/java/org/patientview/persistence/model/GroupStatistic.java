package org.patientview.persistence.model;

import org.patientview.persistence.model.enums.StatisticPeriod;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
@Entity
@Table(name = "group_statistic")
public class GroupStatistic extends BaseModel {

    @OneToOne
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @OneToOne
    @JoinColumn(name = "type_id", nullable = false)
    private Lookup statisticType;

    @Column(name = "value")
    private Long value;

    @Enumerated
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

    public Long getValue() {
        return value;
    }

    public void setValue(final Long value) {
        this.value = value;
    }

    public StatisticPeriod getStatisticPeriod() {
        return statisticPeriod;
    }

    public void setStatisticPeriod(final StatisticPeriod statisticPeriod) {
        this.statisticPeriod = statisticPeriod;
    }
}
