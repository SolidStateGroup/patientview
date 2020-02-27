package org.patientview.persistence.repository;

import org.patientview.persistence.model.QuestionAnswer;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for QuestionAnswer entity
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface QuestionAnswerRepository extends CrudRepository<QuestionAnswer, Long> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM QuestionAnswer q WHERE q.surveyResponse.id = :surveyResponseId")
    void deleteBySurveyResponse(@Param("surveyResponseId") Long surveyResponseId);
}
