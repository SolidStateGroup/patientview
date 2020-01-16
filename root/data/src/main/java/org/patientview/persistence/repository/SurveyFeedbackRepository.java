package org.patientview.persistence.repository;

import org.patientview.persistence.model.Survey;
import org.patientview.persistence.model.SurveyFeedback;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/05/2016
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface SurveyFeedbackRepository extends CrudRepository<SurveyFeedback, Long> {

    @Query("SELECT sf FROM SurveyFeedback sf WHERE sf.survey = :survey AND sf.user = :user")
    List<SurveyFeedback> findBySurveyAndUser(@Param("survey") Survey survey, @Param("user") User user);

    @Modifying(clearAutomatically = true) // note: clearAutomatically required to flush changes straight away
    @Query("DELETE FROM SurveyFeedback WHERE user.id = :userId")
    void deleteSurveyFeedbackByUser(@Param("userId") Long userId);
}
