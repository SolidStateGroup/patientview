package org.patientview.persistence.repository;

import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
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
public interface SurveyResponseRepository extends CrudRepository<SurveyResponse, Long> {

    @Query("SELECT s FROM SurveyResponse s WHERE s.user = :user AND s.survey.type = :surveyType")
    List<SurveyResponse> findByUserAndSurveyType(@Param("user") User user, @Param("surveyType") String surveyType);
}
