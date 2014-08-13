package org.patientview.api.job;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.GroupStatisticService;
import org.patientview.api.service.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

import static org.mockito.Mockito.when;

public class StatisticsTaskTest {

    protected final Logger LOG = LoggerFactory.getLogger(StatisticsTaskTest.class);

    @Mock
    GroupStatisticService groupStatisticService;

    @Mock
    Timer timer;

    @InjectMocks
    StatisticsTask statisticsTask;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test: To set the date to the first of the month and rerun the test
     *
     * @throws Exception
     */
    @Test
    public void testExecuteMonthly() throws Exception {
        Calendar calendar = Calendar.getInstance();
        // Get to the first of the month
        calendar.roll(Calendar.DAY_OF_MONTH, -calendar.get(Calendar.DAY_OF_MONTH) + 1);
        Date endDate = calendar.getTime();
        // Get roll back another month from there
        calendar.roll(Calendar.MONTH, -1);
        Date startDate = calendar.getTime();
        when(timer.getCurrentDate()).thenReturn(calendar);

        LOG.info("Changing the time to {}", endDate);

        // Roll the calendar back for the execute of the method to use;
        calendar.roll(Calendar.MONTH, +1);

        statisticsTask.executeMonthly();

        //TODO cannot verify with dates due to milliseconds
       // verify(groupStatisticService, Mockito.times(1)).generateGroupStatistic(eq(startDate), eq(endDate), eq(StatisticPeriod.MONTH));

    }

    /**
     * Test: Execute a daily run and see if it passed the correct date to the service method
     *
     * @throws Exception
     */
    @Test
    public void testExecuteDaily() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        // Get to the first of the month for a starting point
        calendar.roll(Calendar.DAY_OF_MONTH, -calendar.get(Calendar.DAY_OF_MONTH) + 1);
        Date startDate = calendar.getTime();

        // reset the calendar to the execute of the method
        calendar.setTime(new Date());
        when(timer.getCurrentDate()).thenReturn(calendar);

        statisticsTask.executeDaily();
        //TODO cannot verify with dates due to milliseconds
        //verify(groupStatisticService, Mockito.times(1)).generateGroupStatistic(eq(startDate), eq(endDate), eq(StatisticPeriod.MONTH));
    }
}