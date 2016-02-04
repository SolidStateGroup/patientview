package org.patientview.persistence.repository;

import org.patientview.persistence.model.GpMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface GpMasterRepository extends JpaRepository<GpMaster, Long> {

    @Query("SELECT   gm " +
            "FROM    GpMaster gm " +
            "WHERE   gm.practiceCode = :practiceCode ")
    List<GpMaster> findByPracticeCode(@Param("practiceCode") String practiceCode);

    @Query("SELECT   gm " +
            "FROM    GpMaster gm " +
            "WHERE   gm.postcode = :postcode ")
    List<GpMaster> findByPostcode(@Param("postcode") String gppostcode);
}
