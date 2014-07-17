package org.patientview.persistence.repository;

import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/07/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface IdentifierRepository extends JpaRepository<Identifier, Long> {

    @Modifying
    @Query("DELETE FROM Identifier i WHERE i.user = :user")
    void deleteByUser(@Param("user") User user);
}
