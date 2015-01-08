package org.patientview.persistence.repository;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/09/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ObservationHeadingGroupRepository extends CrudRepository<ObservationHeadingGroup, Long> {

    @Query("SELECT ohg FROM ObservationHeadingGroup ohg WHERE ohg.group = :group")
    List<ObservationHeadingGroup> findByGroup(@Param("group") Group group);
}
