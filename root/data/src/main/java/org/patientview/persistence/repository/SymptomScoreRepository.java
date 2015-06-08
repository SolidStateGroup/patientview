package org.patientview.persistence.repository;

import org.patientview.persistence.model.SymptomScore;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * This gets all the symptom scores associated with a user.
 *
 * Created by james@solidstategroup.com
 * Created on 05/06/2015
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface SymptomScoreRepository extends CrudRepository<SymptomScore, Long> {

    @Query("SELECT s FROM SymptomScore s WHERE s.user = :user")
    public List<SymptomScore> findByUser(@Param("user") User user);
}
