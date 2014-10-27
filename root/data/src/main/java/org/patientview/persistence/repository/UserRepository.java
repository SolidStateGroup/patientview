package org.patientview.persistence.repository;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT CASE WHEN (count(user) > 0) THEN TRUE ELSE FALSE END " +
            "FROM User user WHERE user.username = :username")
    public boolean usernameExists(@Param("username") String username);

    @Query("SELECT CASE WHEN (count(user) > 0) THEN TRUE ELSE FALSE END " +
            "FROM User user WHERE user.email = :email")
    public boolean emailExists(@Param("email") String email);

    User findByUsername(String username);

    User findByEmail(String email);

    @Query("SELECT DISTINCT u " +
           "FROM User u " +
           "JOIN u.groupRoles gr " +
           "LEFT JOIN u.identifiers i " +
           "WHERE gr.role.id IN :roleIds AND gr.group.id IN :groupIds " +
           "AND ((UPPER(u.username) LIKE :filterText) " +
           "OR (UPPER(u.forename) LIKE :filterText) " +
           "OR (UPPER(u.surname) LIKE :filterText) " +
           "OR (UPPER(u.email) LIKE :filterText) " +
           "OR (UPPER(i.identifier) LIKE :filterText)) ")
    Page<User> findByGroupsRoles(@Param("filterText") String filterText,
                                 @Param("groupIds") List<Long> groupIds,
                                 @Param("roleIds") List<Long> roleIds,
                                 Pageable pageable);

    @Query("SELECT DISTINCT u " +
            "FROM User u " +
            "JOIN u.groupRoles gr " +
            "JOIN u.userFeatures uf " +
            "WHERE gr.group = :userGroup " +
            "AND uf.feature = :feature")
    List<User> findByGroupAndFeature(@Param("userGroup") Group userGroup, @Param("feature") Feature feature);

    @Query("SELECT DISTINCT u " +
           "FROM User u " +
           "JOIN u.groupRoles gr " +
           "LEFT JOIN u.identifiers i " +
           "JOIN u.userFeatures uf " +
           "JOIN uf.feature f " +
           "WHERE gr.role.id IN :roleIds " +
           "AND gr.group.id IN :groupIds " +
           "AND f.id IN :featureIds " +
           "AND ((UPPER(u.username) LIKE :filterText) " +
           "OR (UPPER(u.forename) LIKE :filterText) " +
           "OR (UPPER(u.surname) LIKE :filterText) " +
           "OR (UPPER(u.email) LIKE :filterText) " +
           "OR (UPPER(i.identifier) LIKE :filterText)) ")
    Page<User> findByGroupsRolesFeatures(@Param("filterText") String filterText,
                                 @Param("groupIds") List<Long> groupIds,
                                 @Param("roleIds") List<Long> roleIds,
                                 @Param("featureIds") List<Long> featureIds,
                                 Pageable pageable);

}
