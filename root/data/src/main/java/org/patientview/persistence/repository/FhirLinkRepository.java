package org.patientview.persistence.repository;

import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 29/08/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface FhirLinkRepository extends CrudRepository<FhirLink, Long> {

    @Query("SELECT  f " +
            "FROM   FhirLink f " +
            "WHERE  f.user = :user " +
            "AND    f.identifier = :identifier " +
            "AND    f.group = :group " +
            "ORDER BY f.created DESC")
    List<FhirLink> findByUserAndGroupAndIdentifier(@Param("user") User user,
                                                   @Param("group") Group group,
                                                   @Param("identifier") Identifier identifier);

    @Query("SELECT  f " +
            "FROM   FhirLink f " +
            "WHERE  f.user = :user " +
            "AND    f.active = true " +
            "ORDER BY f.created DESC")
    List<FhirLink> findActiveByUser(@Param("user") User user);

    @Query("SELECT  f " +
            "FROM   FhirLink f " +
            "WHERE  f.versionId = :versionId ")
    FhirLink findByVersionUuid(@Param("versionId") UUID versionId);
}
