package org.patientview.persistence.repository;

import org.patientview.persistence.model.Audit;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 05/08/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface AuditRepository extends CrudRepository<Audit, Long> {
}
