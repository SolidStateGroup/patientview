package org.patientview.persistence.repository;

import org.patientview.persistence.model.RelapseMedication;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRUD repository for Relapse medication entity
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface RelapseMedicationRepository extends CrudRepository<RelapseMedication, Long> {

    @Modifying(clearAutomatically = true) // note: clearAutomatically required to flush changes straight away
    @Query("DELETE FROM RelapseMedication WHERE relapse.id = :relapseId")
    void deleteByRelapse(@Param("relapseId") Long relapseId);
}
