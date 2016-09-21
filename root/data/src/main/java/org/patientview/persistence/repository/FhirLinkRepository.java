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
            "AND    f.active = true " +
            "ORDER BY f.created DESC")
    List<FhirLink> findActiveByUser(@Param("user") User user);

    @Query("SELECT DISTINCT f " +
            "FROM FhirLink f " +
            "WHERE f.group IN :groups ")
    List<FhirLink> findByGroups(@Param("groups") List<Group> groups);

    @Query("SELECT DISTINCT f " +
            "FROM FhirLink f " +
            "WHERE f.group IN :groups " +
            "AND (f.user.lastLogin > :date OR f.user.currentLogin > :date)")
    List<FhirLink> findByGroupsAndRecentLogin(@Param("groups") List<Group> groups, @Param("date") Date date);

    List<FhirLink> findByUserAndGroup(User entityUser, Group entityGroup);

    @Query("SELECT  f " +
            "FROM   FhirLink f " +
            "WHERE  f.user = :user " +
            "AND    f.identifier = :identifier " +
            "AND    f.group = :group " +
            "ORDER BY f.created DESC")
    List<FhirLink> findByUserAndGroupAndIdentifier(@Param("user") User user,
                                                   @Param("group") Group group,
                                                   @Param("identifier") Identifier identifier);

    @Query("SELECT  f.user " +
            "FROM   FhirLink f " +
            "WHERE  f.resourceId IN :resourceIds ")
    List<User> findFhirLinkUsersByResourceIds(@Param("resourceIds") List<UUID> resourceIds);
}
