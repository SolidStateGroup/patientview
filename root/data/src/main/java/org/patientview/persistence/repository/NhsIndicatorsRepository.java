package org.patientview.persistence.repository;

import org.patientview.persistence.model.NhsIndicators;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 16/09/16
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
public interface NhsIndicatorsRepository extends JpaRepository<NhsIndicators, Long> {

    @Query("SELECT   i " +
            "FROM    NhsIndicators i " +
            "WHERE   i.groupId = :groupId ")
    List<NhsIndicators> findByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT i FROM NhsIndicators i WHERE i.groupId = :groupId AND i.created = :date")
    NhsIndicators findByGroupIdAndDate(@Param("groupId") Long groupId, @Param("date") Date date);

    @Query("SELECT DISTINCT i.created FROM NhsIndicators i")
    List<Date> getDates();
}
