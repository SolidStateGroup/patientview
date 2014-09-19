package org.patientview.persistence.repository;

import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Lookup;
import org.springframework.data.jpa.repository.JpaRepository;
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

    @Query("SELECT  i " +
           "FROM   Identifier i " +
           "WHERE  i.identifier = :value " +
           "AND    i.identifierType = :type")
    Identifier findByTypeAndValue(@Param("value") String value, @Param("type") Lookup identifierType);

    @Query("SELECT  i " +
           "FROM   Identifier i " +
           "WHERE  i.identifier = :value ")
    Identifier findByValue(@Param("value") String value);
}
