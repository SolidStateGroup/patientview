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

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 29/08/2014
 */
@Repository
@Transactional(propagation = Propagation.REQUIRED)
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
            "AND    f.identifier.identifier = :identifierText " +
            "AND    f.group = :group " +
            "ORDER BY f.created DESC")
    List<FhirLink> findByUserAndGroupAndIdentifierText(@Param("user") User user,
                                                       @Param("group") Group group,
                                                       @Param("identifierText") String identifierText);

    @Query("SELECT  f " +
            "FROM   FhirLink f " +
            "WHERE  f.user = :user " +
            "AND    f.active = true " +
            "ORDER BY f.created DESC")
    List<FhirLink> findActiveByUser(@Param("user") User user);

    @Query("SELECT  f " +
            "FROM   FhirLink f " +
            "WHERE  f.user = :user " +
            "AND f.group = :group " +
            "AND    f.active = false " +
            "ORDER BY f.created DESC")
    List<FhirLink> findInActiveByUserAndGroup(@Param("user") User user, @Param("group") Group group);

    @Query("SELECT new FhirLink(f.id as id, f.user as user) " +
            "FROM FhirLink f " +
            "WHERE f.group = :group " +
            "AND (f.user.lastLogin > :date OR f.user.currentLogin > :date )" +
            "")
    List<FhirLink> testFindByGroupAndRecentLogin(@Param("group") Group group, @Param("date") Date date);

    @Query("SELECT  f " +
            "FROM   FhirLink f " +
            "WHERE  f.versionId = :versionId ")
    FhirLink findByVersionUuid(@Param("versionId") UUID versionId);

    List<FhirLink> findByUserAndGroup(User entityUser, Group entityGroup);

    @Query("SELECT  f.user " +
            "FROM   FhirLink f " +
            "WHERE  f.resourceId IN :resourceIds ")
    List<User> findFhirLinkUsersByResourceIds(@Param("resourceIds") List<UUID> resourceIds);
}
