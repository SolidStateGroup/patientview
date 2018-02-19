package org.patientview.persistence.repository;

import org.patientview.persistence.model.ResearchStudy;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This gets all the news associated with a user. News can be link via
 * group or role.
 *
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ResearchStudyRepository extends CrudRepository<ResearchStudy, Long> {



    @Query("SELECT r " +
            "FROM   ResearchStudy r "
             )
    List<ResearchStudy> findByUser();
}
