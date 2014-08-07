package org.patientview.api.job;

import org.patientview.api.service.GroupStatisticService;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.LookupTypeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.Set;

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

    @Inject
    private LookupTypeRepository lookupTypeRepository;

    @Inject
    private GroupStatisticService groupStatisticService;

    @Inject
    private EntityManager entityManager;

    /**
     * The days statistics should be collated the day after.
     * The monthly statistics should be collated on the first day of the following month.
     *
     *
     */
    @Scheduled(cron = "1 0 0 0 0")
    public void executeMonthly() {
        //TODO Sprint 3, current the only way to get a list of enums. StatisticType and RoleType are redundant
        Set<Lookup> lookups = lookupTypeRepository.findByType(LookupTypes.STATISTICS_TYPE).getLookups();

    }

    private void collateMonthlyStatistics() {

    }




}
