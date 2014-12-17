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

    List<User> findByEmail(String email);

        /*
        SQL for AND group ids

        SELECT
          u.ID, u.surname, count(UGR.group_ID)
          FROM pv_user u
         JOIN pv_user_group_role UGR ON u.ID = UGR.user_ID
         WHERE UGR.role_id IN (1)
        AND UGR.group_ID IN (2467239,2467263)
        AND ((UPPER(u.username) LIKE '%E%')
        OR (UPPER(u.forename) LIKE '%E%')
        OR (UPPER(u.surname) LIKE '%E%')
        OR (UPPER(u.email) LIKE '%E%') )
         GROUP BY u.ID, u.surname
         HAVING Count(UGR.group_id) = 2
     */

    @Query("SELECT u " +
           "FROM User u " +
           "JOIN u.groupRoles gr " +
           "JOIN u.identifiers i " +
           "WHERE gr.role.id IN :roleIds " +
           "AND gr.group.id IN :groupIds " +
           "AND (UPPER(u.username) LIKE :searchUsername) " +
           "AND (UPPER(u.forename) LIKE :searchForename) " +
           "AND (UPPER(u.surname) LIKE :searchSurname) " +
           "AND (UPPER(u.email) LIKE :searchEmail) " +
           "AND (i IN (SELECT id FROM Identifier id WHERE UPPER(id.identifier) LIKE :searchIdentifier)) " +
            "GROUP BY u.id " +
            "HAVING COUNT(gr) = :groupCount")
    Page<User> findPatientByGroupsRolesAnd(@Param("searchUsername") String searchUsername,
                                @Param("searchForename") String searchForename,
                                @Param("searchSurname") String searchSurname,
                                @Param("searchEmail") String searchEmail,
                                @Param("searchIdentifier") String searchIdentifier,
                                @Param("groupIds") List<Long> groupIds,
                                @Param("roleIds") List<Long> roleIds,
                                @Param("groupCount") Long groupCount,
                                Pageable pageable);

    @Query("SELECT u " +
           "FROM User u " +
           "JOIN u.groupRoles gr " +
           "WHERE gr.role.id IN :roleIds " +
           "AND (gr.group.id) IN (:groupIds) " +
           "AND (UPPER(u.username) LIKE :searchUsername) " +
           "AND (UPPER(u.forename) LIKE :searchForename) " +
           "AND (UPPER(u.surname) LIKE :searchSurname) " +
           "AND (UPPER(u.email) LIKE :searchEmail) " +
           "GROUP BY u.id " +
            "HAVING COUNT(gr) = :groupCount")
    Page<User> findStaffByGroupsRolesAnd(@Param("searchUsername") String searchUsername,
                                 @Param("searchForename") String searchForename,
                                 @Param("searchSurname") String searchSurname,
                                 @Param("searchEmail") String searchEmail,
                                 @Param("groupIds") List<Long> groupIds,
                                 @Param("roleIds") List<Long> roleIds,
                                 @Param("groupCount") Long groupCount,
                                 Pageable pageable);

    @Query("SELECT u " +
            "FROM User u " +
            "JOIN u.groupRoles gr " +
            "JOIN u.identifiers i " +
            "WHERE gr.role.id IN :roleIds " +
            "AND gr.group.id IN :groupIds " +
            "AND (UPPER(u.username) LIKE :searchUsername) " +
            "AND (UPPER(u.forename) LIKE :searchForename) " +
            "AND (UPPER(u.surname) LIKE :searchSurname) " +
            "AND (UPPER(u.email) LIKE :searchEmail) " +
            "AND (i IN (SELECT id FROM Identifier id WHERE UPPER(id.identifier) LIKE :searchIdentifier)) " +
            "GROUP BY u.id")
    Page<User> findPatientByGroupsRoles(@Param("searchUsername") String searchUsername,
                                        @Param("searchForename") String searchForename,
                                        @Param("searchSurname") String searchSurname,
                                        @Param("searchEmail") String searchEmail,
                                        @Param("searchIdentifier") String searchIdentifier,
                                        @Param("groupIds") List<Long> groupIds,
                                        @Param("roleIds") List<Long> roleIds,
                                        Pageable pageable);

    @Query("SELECT u " +
            "FROM User u " +
            "JOIN u.groupRoles gr " +
            "WHERE gr.role.id IN :roleIds " +
            "AND gr.group.id IN :groupIds " +
            "AND (UPPER(u.username) LIKE :searchUsername) " +
            "AND (UPPER(u.forename) LIKE :searchForename) " +
            "AND (UPPER(u.surname) LIKE :searchSurname) " +
            "AND (UPPER(u.email) LIKE :searchEmail) " +
            "GROUP BY u.id")
    Page<User> findStaffByGroupsRoles(@Param("searchUsername") String searchUsername,
                                      @Param("searchForename") String searchForename,
                                      @Param("searchSurname") String searchSurname,
                                      @Param("searchEmail") String searchEmail,
                                      @Param("groupIds") List<Long> groupIds,
                                      @Param("roleIds") List<Long> roleIds,
                                      Pageable pageable);
    @Query("SELECT u " +
            "FROM User u " +
            "JOIN u.groupRoles gr " +
            "JOIN u.identifiers i " +
            "WHERE gr.role.id IN :roleIds " +
            "AND gr.group.id IN :groupIds " +
            "GROUP BY u.id")
    Page<User> findPatientByGroupsRolesNoFilter(@Param("groupIds") List<Long> groupIds,
                                        @Param("roleIds") List<Long> roleIds,
                                        Pageable pageable);

    @Query("SELECT u " +
            "FROM User u " +
            "JOIN u.groupRoles gr " +
            "WHERE gr.role.id IN :roleIds " +
            "AND gr.group.id IN :groupIds " +
            "GROUP BY u.id")
    Page<User> findStaffByGroupsRolesNoFilter(@Param("groupIds") List<Long> groupIds,
                                      @Param("roleIds") List<Long> roleIds,
                                      Pageable pageable);

    @Query("SELECT u " +
            "FROM User u " +
            "JOIN u.groupRoles gr " +
            "WHERE (gr.group.id) IN (:groupIds) " +
            "GROUP BY u.id " +
            "HAVING COUNT(gr) = :groupCount")
    List<User> findGroupTest(@Param("groupIds") List<Long> groupIds,
                             @Param("groupCount") Long groupCount);

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
