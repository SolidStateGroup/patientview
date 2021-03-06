package org.patientview.persistence.repository;

import org.patientview.persistence.model.InsDiaryRecord;
import org.patientview.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD repository for InsDiaryRecord entity
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface InsDiaryRepository extends CrudRepository<InsDiaryRecord, Long> {

    @Query("SELECT d FROM InsDiaryRecord d WHERE d.user = :user ORDER BY entryDate DESC")
    Page<InsDiaryRecord> findByUser(@Param("user") User user, Pageable pageable);

    // used internally
    @Query(value = "SELECT d FROM InsDiaryRecord d WHERE d.user = :user")
    List<InsDiaryRecord> findListByUser(@Param("user") User user, Pageable pageable);

    @Modifying(clearAutomatically = true) // note: clearAutomatically required to flush changes straight away
    @Query("DELETE FROM InsDiaryRecord WHERE user.id = :userId")
    void deleteByUser(@Param("userId") Long userId);
}
