package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.GroupStatisticTO;
import org.patientview.api.service.GroupStatisticService;
import org.patientview.api.service.Timer;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RESTful interface for managing Group statistics.
 *
 * Created by james@solidstategroup.com
 * Created on 03/07/2017
 */
@RestController
@ExcludeFromApiDoc
public class GroupStatisticsController extends BaseController<GroupStatisticsController> {

    private static final Logger LOG = LoggerFactory.getLogger(GroupStatisticsController.class);

    @Inject
    private GroupStatisticService groupStatisticService;

    @Inject
    private Timer timer;

    /**
     * Get statistics for a Group given an ID.
     * @param groupId ID of the Group to retrieve statistics for
     * @return List of GroupStatisticTO objects with monthly statistics for a Group
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/group/{groupId}/statistics", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<GroupStatisticTO> getStatistics(@PathVariable("groupId") Long groupId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return groupStatisticService.getMonthlyGroupStatistics(groupId);
    }

    /**
     * Global admin only, generate monthly statistics in same way as scheduled task.
     */
    @RequestMapping(value = "/admin/group/generatemonthlystatistics", method = RequestMethod.GET)
    @ResponseBody
    public void generateMonthlyStatistics() {
        Calendar calendar = timer.getCurrentDate();

        // set end date to now
        Date endDate = calendar.getTime();

        // Set the start date to the beginning of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();

        LOG.info("Creating statistics for period " + startDate.toString() + " to " + endDate.toString());
        Date startTask = new Date();

        try {
            groupStatisticService.generateGroupStatisticAdminOnly(startDate, endDate, StatisticPeriod.MONTH);
            LOG.info("Created statistics for period " + startDate.toString()
                    + " to " + endDate.toString() + " took "
                    + getDateDiff(startTask, new Date(), TimeUnit.SECONDS) + " seconds.");
        } catch (Exception e) {
            LOG.error("Error creating statistics for period " + startDate.toString()
                    + " to " + endDate.toString() + ": " + e.getMessage() + ", took "
                    + getDateDiff(startTask, new Date(), TimeUnit.SECONDS) + " seconds.", e);
        }
    }

    private long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMilliseconds = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMilliseconds, TimeUnit.MILLISECONDS);
    }
}
