package org.patientview.persistence.repository;

import org.patientview.persistence.model.Audit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 05/08/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface AuditRepository extends CrudRepository<Audit, Long> {

    public List<Audit> findAll();

    @Query("SELECT a " +
            "FROM Audit a ")
    Page<Audit> findAllFiltered(Pageable pageable);
}
