package org.patientview.persistence.repository;

import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.enums.StatisticPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 07/08/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface GroupStatisticRepository extends JpaRepository<GroupStatistic, Long> {

    Iterable<GroupStatistic> findByGroupIdAndStatisticPeriod(Long groupId, StatisticPeriod month);

}
