package org.patientview.persistence.repository;

import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.LookupType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LookupRepository extends CrudRepository<Lookup, Long>  {

    @Query("SELECT loo FROM Lookup loo WHERE loo.lookupType = :lookupType")
    public Iterable<Lookup> findByType(@Param("lookupType") LookupType lookupType);

    @Query("SELECT loo FROM Lookup loo WHERE loo.lookupType.type = :lookupType AND loo.value = :lookupValue")
    public Lookup findByTypeAndValue(
            @Param("lookupType") String lookupType, @Param("lookupValue") String lookupValue);
}
