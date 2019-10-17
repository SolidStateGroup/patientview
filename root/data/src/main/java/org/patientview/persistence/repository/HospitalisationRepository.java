package org.patientview.persistence.repository;

import org.patientview.persistence.model.Hospitalisation;
import org.patientview.persistence.model.InsDiaryRecord;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD repository for Hospitalisation entity
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface HospitalisationRepository extends CrudRepository<Hospitalisation, Long> {

    @Query("SELECT h FROM Hospitalisation h WHERE h.user = :user ORDER BY h.dateAdmitted DESC")
    List<InsDiaryRecord> findByUser(@Param("user") User user);
}
