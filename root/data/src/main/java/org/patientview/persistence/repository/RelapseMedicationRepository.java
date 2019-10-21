package org.patientview.persistence.repository;

import org.patientview.persistence.model.RelapseMedication;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD repository for Relapse medication entity
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface RelapseMedicationRepository extends CrudRepository<RelapseMedication, Long> {

}
