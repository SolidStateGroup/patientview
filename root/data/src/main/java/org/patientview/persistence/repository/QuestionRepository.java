package org.patientview.persistence.repository;

import org.patientview.persistence.model.Question;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/06/2015
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface QuestionRepository extends CrudRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.type = :questionType")
    Iterable<Question> findByType(@Param("questionType") String type);
}
