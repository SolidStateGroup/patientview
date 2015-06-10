package org.patientview.persistence.repository;

import org.patientview.persistence.model.SymptomScore;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.SurveyTypes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2015
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface SymptomScoreRepository extends CrudRepository<SymptomScore, Long> {

    @Query("SELECT s FROM SymptomScore s WHERE s.user = :user")
    public List<SymptomScore> findByUser(@Param("user") User user);

    @Query("SELECT s FROM SymptomScore s WHERE s.user = :user AND s.survey.type = :surveyType")
    List<SymptomScore> findByUserAndSurveyType(@Param("user") User user, @Param("surveyType") SurveyTypes surveyType);
}
