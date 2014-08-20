package org.patientview.persistence.repository;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface GroupStatisticRepository extends JpaRepository<GroupStatistic, Long> {

    Iterable<GroupStatistic> findByGroupAndStatisticPeriod(Group group, StatisticPeriod month);

    @Modifying
    @Query("DELETE FROM GroupStatistic WHERE group = :group AND startDate = :startDate AND statisticPeriod = :statisticPeriod")
    void deleteByGroupStartDateAndPeriod(@Param("group") Group group,
                                         @Param("startDate") Date startDate,
                                         @Param("statisticPeriod") StatisticPeriod statisticPeriod);

}
