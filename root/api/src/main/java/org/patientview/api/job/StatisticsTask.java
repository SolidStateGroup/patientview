package org.patientview.api.job;

import org.patientview.api.service.GroupStatisticService;
import org.patientview.api.service.Timer;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

/**
 * This is the class that executes the job to collate the stats.
 *
 * Sprint 3 - after this is seems Lookup/GenericLookup types need reimplementing.
 *
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
@Component
public class StatisticsTask {

    protected final Logger LOG = LoggerFactory.getLogger(StatisticsTask.class);

    @Inject
    private Timer timer;

    @Inject
    private GroupStatisticService groupStatisticService;

    /**
     * TODO this needs hardening and possibly JMX bean to run the job
     * The days statistics should be collated the day after.
     * The monthly statistics should be collated on the first day of the following month.
     */
    //@Scheduled(cron = "0 0 12 1 * *") -- 12 oclock of the first day of the month
    public void executeMonthly() {
        LOG.info("Executing monthly");
        Calendar calendar = timer.getCurrentDate();
        // Run of the first day of the month for last month
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            Date endDate = calendar.getTime();
            calendar.roll(Calendar.MONTH, -1);
            Date startDate = calendar.getTime();
            groupStatisticService.generateGroupStatistic(startDate, endDate, StatisticPeriod.CUMULATIVE_MONTH);
        }
    }

    /**
     * Cumulative stats for the month, run once a day.
     */
    //@Scheduled(cron = "*/5 * * * * *") //every five minutes
    @Scheduled(fixedRate = 5000L)
    public void executeDaily() {
        LOG.info("Executing daily statistics");
        Calendar calendar = timer.getCurrentDate();

        // Run of the first day of the month for last month
        Date endDate = calendar.getTime();

        // Set the start date to the beginning of the month
        calendar.roll(Calendar.DAY_OF_MONTH, -calendar.get(Calendar.DAY_OF_MONTH) + 1);

        Date startDate = calendar.getTime();
        groupStatisticService.generateGroupStatistic(startDate, endDate, StatisticPeriod.MONTH);
    }
}




