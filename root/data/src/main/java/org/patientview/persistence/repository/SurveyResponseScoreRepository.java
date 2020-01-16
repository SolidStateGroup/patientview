package org.patientview.persistence.repository;

import org.patientview.persistence.model.SurveyResponseScore;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for SurveyResponseScore entity
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface SurveyResponseScoreRepository extends CrudRepository<SurveyResponseScore, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM SurveyResponseScore s WHERE s.surveyResponse.id = :surveyResponseId")
    void deleteBySurveyResponse(@Param("surveyResponseId") Long surveyResponseId);
}
