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
            "FROM User user WHERE UPPER(user.username) = UPPER(:username)")
    public boolean usernameExistsCaseInsensitive(@Param("username") String username);

    @Query("SELECT CASE WHEN (count(user) > 0) THEN TRUE ELSE FALSE END " +
            "FROM User user WHERE user.email = :email")
    public boolean emailExists(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE UPPER(u.username) = UPPER(:username)")
    User findByUsernameCaseInsensitive(@Param("username") String username);

    User findByEmail(String email);

    @Query("select u1 FROM User u1 " +
            "JOIN u1.groupRoles gr1 " +
            "WHERE u1 IN (" +

            "SELECT u " +
           "FROM User u " +
           "JOIN u.groupRoles gr " +
           "JOIN u.identifiers i " +
           "WHERE gr.role.id IN :roleIds " +
           "AND gr.group.id IN :groupIds " +
           "AND ((UPPER(u.username) LIKE :filterText) " +
           "OR (UPPER(u.forename) LIKE :filterText) " +
           "OR (UPPER(u.surname) LIKE :filterText) " +
           "OR (UPPER(u.email) LIKE :filterText) " +
           "OR (i IN (SELECT id FROM Identifier id WHERE UPPER(id.identifier) LIKE :filterText))) " +
            "GROUP BY u.id" +

            ") GROUP BY u1 HAVING Count(gr1.group.id) = :groupCount")
    Page<User> findPatientByGroupsRoles(@Param("filterText") String filterText,
                                 @Param("groupIds") List<Long> groupIds,
                                 @Param("roleIds") List<Long> roleIds,
                                 @Param("groupCount") Long groupCount,
                                 Pageable pageable);

    @Query(//"select u1 FROM User u1 " +
           // "JOIN u1.groupRoles gr1 " +
           // "WHERE u1 IN (" +
            
            "SELECT u " +
           "FROM User u " +
           "JOIN u.groupRoles gr " +
           "WHERE gr.role.id IN :roleIds " +
           "AND (:groupIds) IN (gr.group.id) " +
           "AND ((UPPER(u.username) LIKE :filterText) " +
           "OR (UPPER(u.forename) LIKE :filterText) " +
           "OR (UPPER(u.surname) LIKE :filterText) " +
           "OR (UPPER(u.email) LIKE :filterText)) " +
           "GROUP BY u.id")// +

           //") GROUP BY u1 HAVING Count(gr1.group.id) = :groupCount")
    Page<User> findStaffByGroupsRoles(@Param("filterText") String filterText,
                                 @Param("groupIds") List<Long> groupIds,
                                 @Param("roleIds") List<Long> roleIds,
                                 //@Param("groupCount") Long groupCount,
                                 Pageable pageable);
    @Query("SELECT u " +
            "FROM User u " +
            "JOIN u.groupRoles gr " +
            "WHERE (:groupIds) IN (gr.group.id) " +
            "GROUP BY u.id")
        //List<User> findGroupTest(@Param("groupIds") List<Long> groupIds);
    List<User> findGroupTest(@Param("groupIds") List<Long> groupIds);

    @Query("SELECT u " +
            "FROM User u " +
            "JOIN u.groupRoles gr " +
            "JOIN u.userFeatures uf " +
            "WHERE gr.group = :userGroup " +
            "AND uf.feature = :feature " +
            "GROUP BY u.id")
    List<User> findByGroupAndFeature(@Param("userGroup") Group userGroup, @Param("feature") Feature feature);

    @Query("SELECT u " +
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
           "OR (UPPER(u.email) LIKE :filterText)) " +
            "GROUP BY u.id")
    Page<User> findStaffByGroupsRolesFeatures(@Param("filterText") String filterText,
                                 @Param("groupIds") List<Long> groupIds,
                                 @Param("roleIds") List<Long> roleIds,
                                 @Param("featureIds") List<Long> featureIds,
                                 Pageable pageable);

    @Query("SELECT u " +
           "FROM User u " +
           "JOIN u.groupRoles gr " +
           "JOIN u.identifiers i " +
           "JOIN u.userFeatures uf " +
           "JOIN uf.feature f " +
           "WHERE gr.role.id IN :roleIds " +
           "AND gr.group.id IN :groupIds " +
           "AND f.id IN :featureIds " +
           "AND ((UPPER(u.username) LIKE :filterText) " +
           "OR (UPPER(u.forename) LIKE :filterText) " +
           "OR (UPPER(u.surname) LIKE :filterText) " +
           "OR (UPPER(u.email) LIKE :filterText) " +
           "OR (i IN (SELECT id FROM Identifier id WHERE UPPER(id.identifier) LIKE :filterText))) " +
            "GROUP BY u.id")
    Page<User> findPatientByGroupsRolesFeatures(@Param("filterText") String filterText,
                                 @Param("groupIds") List<Long> groupIds,
                                 @Param("roleIds") List<Long> roleIds,
                                 @Param("featureIds") List<Long> featureIds,
                                 Pageable pageable);

}
