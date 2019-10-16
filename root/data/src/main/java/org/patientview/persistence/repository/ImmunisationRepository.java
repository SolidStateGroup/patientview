package org.patientview.persistence.repository;

import org.patientview.persistence.model.Immunisation;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD repository for Immunisation entity
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ImmunisationRepository extends CrudRepository<Immunisation, Long> {

    @Query("SELECT i FROM Immunisation i WHERE i.user = :user")
    List<Immunisation> findByUser(@Param("user") User user);
}
