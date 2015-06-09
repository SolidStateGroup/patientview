package org.patientview.persistence.repository;

import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 09/06/2015
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface SurveyRepository extends CrudRepository<Survey, Long> {

    @Query("SELECT s " +
            "FROM Survey s " +
            "WHERE s.type = :type ")
    public List<Survey> findByType(@Param("type") SurveyTypes type);
}
