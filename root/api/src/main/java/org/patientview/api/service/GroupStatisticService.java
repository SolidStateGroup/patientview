package org.patientview.api.service;

import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.GroupStatisticTO;
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
 * Group statistic service, used when retrieving and creating Group statistics.
 *
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupStatisticService {

    /**
     * Generate statistics for a Group over a specific time period. Uses SQL stored in LookupValues.
     * @param startDate Start date of statistic period
     * @param endDate End date of statistic period
     * @param statisticPeriod StatisticPeriod enum, typically StatisticPeriod.MONTH
     */
    void generateGroupStatistic(Date startDate, Date endDate, StatisticPeriod statisticPeriod);

    /**
     * Helper function to generate statistics from controller only as global admin. Calls generateGroupStatistic().
     * @param startDate Start date of statistic period
     * @param endDate End date of statistic period
     * @param statisticPeriod StatisticPeriod enum, typically StatisticPeriod.MONTH
     */
    @RoleOnly
    void generateGroupStatisticAdminOnly(Date startDate, Date endDate, StatisticPeriod statisticPeriod);

    /**
     * Get statistics for a Group given an ID.
     * @param groupId ID of the Group to retrieve statistics for
     * @return List of GroupStatisticTO objects with monthly statistics for a Group
     * @throws ResourceNotFoundException thrown when Group does not exist
     * @throws ResourceForbiddenException thrown when User does not have required permissions
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN,
            RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN, RoleName.GP_ADMIN })
    List<GroupStatisticTO> getMonthlyGroupStatistics(Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    // migration only
    @GroupMemberOnly
    void migrateStatistics(Long groupId, List<GroupStatistic> statistics) throws ResourceNotFoundException;
}
