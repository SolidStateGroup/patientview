package org.patientview.persistence.repository;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Link;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface LinkRepository extends CrudRepository<Link, Long> {

    @Modifying
    @Query("DELETE FROM Link l WHERE link.group = :group")
    void deleteByGroup(@Param("group") Group group);

    @Modifying
    @Query("DELETE FROM Link l WHERE link.id = :linkId")
    void deleteById(@Param("linkId") Long linkId);
}
