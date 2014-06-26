package org.patientview.persistence.repository;

import org.patientview.persistence.model.LookupType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 20/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LookupTypeRepository extends CrudRepository<LookupType, Long> {

    @Query("SELECT lut FROM LookupType lut WHERE lut.type = :type")
    public LookupType getByType(@Param("type") String type);
}
