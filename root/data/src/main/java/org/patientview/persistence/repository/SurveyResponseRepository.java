package org.patientview.persistence.repository;

import org.patientview.persistence.model.SurveyResponse;
import org.patientview.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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

    @Query("SELECT s FROM SurveyResponse s WHERE s.user = :user AND s.survey.type = :surveyType AND s.date = :date")
    List<SurveyResponse> findByUserAndSurveyTypeAndDate(@Param("user") User user,
                                                    @Param("surveyType") String surveyType, @Param("date") Date date);

    @Query("SELECT s FROM SurveyResponse s WHERE s.user = :user AND s.survey.type = :surveyType ORDER BY s.date DESC")
    Page<SurveyResponse> findLatestByUserAndSurveyType(@Param("user") User user, @Param("surveyType") String surveyType,
                                                 Pageable pageable);

    List<SurveyResponse> findByDateBetweenAndSurveyIn(Date start, Date end, List<String> surveyType);
}
