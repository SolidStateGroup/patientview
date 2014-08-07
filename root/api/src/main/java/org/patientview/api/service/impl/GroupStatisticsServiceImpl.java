package org.patientview.api.service.impl;

import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.api.service.GroupStatisticService;
import org.patientview.api.util.Util;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.GroupStatisticRepository;
import org.patientview.persistence.repository.LookupTypeRepository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
public class GroupStatisticsServiceImpl extends AbstractServiceImpl<GroupStatisticsServiceImpl> implements GroupStatisticService {

    @Inject
    private GroupStatisticRepository groupStatisticRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private LookupTypeRepository lookupTypeRepository;

    @Inject
    private EntityManager entityManager;

    /**
     * Summary view of the statistics month by month
     *
     * @param groupId
     * @return
     * @throws ResourceNotFoundException
     */
    @Override
    public List<GroupStatistic> getMonthGroupStatistics(final Long groupId)
            throws ResourceNotFoundException {

        Group group = groupRepository.findOne(groupId);
        if (group == null) {
            throw new ResourceNotFoundException("The group could not be found");
        }

        List<GroupStatistic> groupStatistics =
                Util.iterableToList(groupStatisticRepository.findByGroupIdAndStatisticPeriod(groupId,
                        StatisticPeriod.MONTH));

        return groupStatistics;
    }

    private List<GroupStatistic> getDayGroupStatistics(final Long groupId, final Date startDate, final Date endDate) {
        return null;
    }


    /**
     * Creates statistics for all the groups. Loop through the statistics and then the groups.
     *
     * @param startDate
     * @param endDate
     * @param statisticPeriod
     */
    public void generateGroupStatistic(Date startDate, Date endDate, StatisticPeriod statisticPeriod) {

        // Create the groupStatistic object which we are going to persist repeatably
        GroupStatistic groupStatistic = new GroupStatistic();
        groupStatistic.setStartDate(startDate);
        groupStatistic.setEndDate(endDate);
        groupStatistic.setStatisticPeriod(statisticPeriod);

        for (Lookup lookup : lookupTypeRepository.findByType(LookupTypes.STATISTICS_TYPE).getLookups()) {

            groupStatistic.setStatisticType(lookup);

            Query query = entityManager.createNativeQuery(lookup.getDescription(), Long.class);
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);

            for (Group group : groupRepository.findAll()) {

                query.setParameter("groupId", group.getId());
                // Set the value and save
                groupStatistic.setValue((Long) query.getSingleResult());
                groupStatisticRepository.save(groupStatistic);
            }
            groupStatistic = createGroupStatistic(groupStatistic);
        }
    }

    private GroupStatistic createGroupStatistic(GroupStatistic groupStatistic) {
        GroupStatistic newGroupStatistic = new GroupStatistic();
        newGroupStatistic.setStartDate(groupStatistic.getStartDate());
        newGroupStatistic.setEndDate(groupStatistic.getEndDate());
        newGroupStatistic.setStartDate(groupStatistic.getStartDate());
        newGroupStatistic.setStatisticPeriod(groupStatistic.getStatisticPeriod());
        return newGroupStatistic;
    }



}
