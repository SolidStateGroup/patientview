package org.patientview.persistence.repository;

import org.patientview.persistence.model.NhschoicesCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 06/06/2016
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface NhschoicesConditionRepository extends JpaRepository<NhschoicesCondition, Long> {
}
