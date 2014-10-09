package org.patientview.api.service;

import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupStatisticService {

    @GroupMemberOnly(roles = { RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN })
    List<GroupStatistic> getMonthlyGroupStatistics(Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    void generateGroupStatistic(Date startDate, Date endDate, StatisticPeriod statisticPeriod);
}

