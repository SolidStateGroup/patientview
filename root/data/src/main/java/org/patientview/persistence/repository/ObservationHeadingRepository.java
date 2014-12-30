package org.patientview.persistence.repository;

import org.patientview.persistence.model.ObservationHeading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface ObservationHeadingRepository extends CrudRepository<ObservationHeading, Long> {

    public Page<ObservationHeading> findAll(Pageable pageable);

    @Query("SELECT new ObservationHeading(oh.id as id, oh.code as code, oh.heading as heading, oh.name as name, " +
            "oh.normalRange as normalRange, " +
            "oh.units as units, oh.minGraph as minGraph, oh.maxGraph as maxGraph, " +
            "oh.infoLink as infoLink) FROM ObservationHeading oh")
    public Page<ObservationHeading> findAllMinimal(Pageable pageable);

    @Query("SELECT oh FROM ObservationHeading oh WHERE oh.code = :code")
    public List<ObservationHeading> findByCode(@Param("code") String code);
}
