package org.patientview.persistence.repository;

import org.patientview.persistence.model.Relapse;
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
 * CRUD repository for Relapse entity
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface RelapseRepository extends CrudRepository<Relapse, Long> {

    @Query("SELECT r FROM Relapse r WHERE r.user = :user")
    List<Relapse> findByUser(@Param("user") User user);

    @Modifying(clearAutomatically = true) // note: clearAutomatically required to flush changes straight away
    @Query("DELETE FROM Relapse WHERE user.id = :userId")
    void deleteByUser(@Param("userId") Long userId);
}
