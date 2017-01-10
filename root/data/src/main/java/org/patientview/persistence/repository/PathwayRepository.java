package org.patientview.persistence.repository;

import org.patientview.persistence.model.Pathway;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pathway JPA repository
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface PathwayRepository extends CrudRepository<Pathway, Long> {

}
