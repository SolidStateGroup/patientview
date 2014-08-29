package org.patientview.persistence.repository;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    User findByUsername(String username);

    User findByEmail(String email);

    @Query("SELECT u FROM User u WHERE username = :username AND password = :password")
    User findByUsernameAndPassword(@Param("username") String username, @Param("password")  String password);

    @Modifying
    @Query("SELECT u FROM User u JOIN u.groupRoles gr WHERE gr.role = :role AND gr.group = :group")
    Iterable<User> findByGroupAndRole(@Param("group") Group group, @Param("role") Role role);

    @Query("SELECT DISTINCT u " +
           "FROM User u " +
           "JOIN u.groupRoles gr " +
           "WHERE gr.role.id IN :roleIds AND gr.group.id IN :groupIds " +
           "AND ((UPPER(u.username) LIKE :filterText) " +
           "OR (UPPER(u.forename) LIKE :filterText) " +
           "OR (UPPER(u.surname) LIKE :filterText) " +
           "OR (UPPER(u.email) LIKE :filterText)) ")
    Page<User> findByGroupsRoles(@Param("filterText") String filterText,
                                 @Param("groupIds") List<Long> groupIds,
                                 @Param("roleIds") List<Long> roleIds,
                                 Pageable pageable);

    @Query("SELECT DISTINCT u " +
           "FROM User u " +
           "JOIN u.groupRoles gr " +
           "JOIN u.userFeatures uf " +
           "JOIN uf.feature f " +
           "WHERE gr.role.id IN :roleIds " +
           "AND gr.group.id IN :groupIds " +
           "AND f.id IN :featureIds " +
           "AND ((UPPER(u.username) LIKE :filterText) " +
           "OR (UPPER(u.forename) LIKE :filterText) " +
           "OR (UPPER(u.surname) LIKE :filterText) " +
           "OR (UPPER(u.email) LIKE :filterText)) ")
    Page<User> findByGroupsRolesFeatures(@Param("filterText") String filterText,
                                 @Param("groupIds") List<Long> groupIds,
                                 @Param("roleIds") List<Long> roleIds,
                                 @Param("featureIds") List<Long> featureIds,
                                 Pageable pageable);
}
