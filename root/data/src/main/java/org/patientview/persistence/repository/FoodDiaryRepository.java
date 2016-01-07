package org.patientview.persistence.repository;

import org.patientview.persistence.model.FoodDiary;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 21/12/2015
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface FoodDiaryRepository extends CrudRepository<FoodDiary, Long> {

    @Query("SELECT fd FROM FoodDiary fd WHERE fd.user = :user")
    List<FoodDiary> findByUser(@Param("user") User user);
}
