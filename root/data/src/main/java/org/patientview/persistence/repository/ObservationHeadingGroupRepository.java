package org.patientview.persistence.repository;

import org.patientview.persistence.model.ObservationHeadingGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/09/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ObservationHeadingGroupRepository extends CrudRepository<ObservationHeadingGroup, Long> {

}
