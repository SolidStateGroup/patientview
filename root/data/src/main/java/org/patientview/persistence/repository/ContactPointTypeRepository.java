package org.patientview.persistence.repository;

import org.patientview.persistence.model.ContactPointType;
import org.patientview.persistence.model.enums.ContactPointTypes;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 10/02/2016
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ContactPointTypeRepository extends LookupRepository {
    @Query("SELECT c FROM ContactPointType c " +
            "WHERE c.value = :value ")
    List<ContactPointType> findByValue(@Param("value") ContactPointTypes value);
}
