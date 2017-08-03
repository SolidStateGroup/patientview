package org.patientview.persistence.repository;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
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
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface GroupRepository extends CrudRepository <Group, Long> {

    @Query("SELECT g FROM Group g " +
            "WHERE (UPPER(g.code) LIKE :filterText) " +
            "OR (UPPER(g.name) LIKE :filterText) ")
    Page<Group> findAll(@Param("filterText") String filterText, Pageable pageable);

    @Query("SELECT g FROM Group g " +
            "WHERE ((UPPER(g.code) LIKE :filterText) " +
            "OR (UPPER(g.name) LIKE :filterText)) " +
            "AND (g.groupType.id IN (:groupTypes))")
    Page<Group> findAllByGroupType(@Param("filterText") String filterText,
                                           @Param("groupTypes") List<Long> groupTypes,
                                           Pageable pageable);

    @Query("SELECT u " +
           "FROM   User u JOIN u.groupRoles gr " +
           "WHERE  gr.group.id = :groupId " +
           "AND    gr.role.roleType.value = :roleType")
    Iterable<User> findGroupStaffByRole(@Param("groupId") Long groupId,
                                               @Param("roleType") String roleType);

    @Query("SELECT gr.group " +
           "FROM   User u " +
           "JOIN   u.groupRoles gr " +
           "WHERE  gr.role = :role " +
           "AND    u = :user")
    Iterable<Group> findGroupByUserAndRole(@Param("user") User user,
                                                  @Param("role") Role role);

    @Query("SELECT gr.group " +
           "FROM   User u " +
           "JOIN   u.groupRoles gr " +
           "WHERE  u = :user " +
           "AND    gr.group.visible = true ")
    Iterable<Group> findGroupByUser(@Param("user") User user);

    @Query("SELECT g " +
            "FROM   Group g " +
            "JOIN   g.groupRoles gr " +
            "WHERE  gr.user = :user " +
            "AND    gr.group.visible = true " +
            "AND    gr.group.groupType.value <> 'SPECIALTY' " +
            "AND ((UPPER(gr.group.code) LIKE :filterText) " +
            "OR (UPPER(gr.group.name) LIKE :filterText)) ")
    Page<Group> findGroupsByUserNoSpecialties(@Param("filterText") String filterText,
                                                     @Param("user") User user, Pageable pageable);

    @Query("SELECT g " +
            "FROM   Group g " +
            "JOIN   g.groupRoles gr " +
            "WHERE  gr.user = :user " +
           "AND    gr.group.visible = true " +
           "AND    gr.group.groupType.value <> 'SPECIALTY' " +
           "AND ((UPPER(gr.group.code) LIKE :filterText) " +
           "OR (UPPER(gr.group.name) LIKE :filterText)) " +
           "AND (gr.group.groupType.id IN (:groupTypes))")
    Page<Group> findGroupsByUserAndGroupTypeNoSpecialties(@Param("filterText") String filterText,
                                                     @Param("groupTypes") List<Long> groupTypes,
                                                     @Param("user") User user, Pageable pageable);

    // get group and children
    // TODO: this query and below need work to work in both postgresql 9.4b1 and 9.4b2
    // TODO: requires DISTINCT in b1 but not b2 but with DISTINCT fails to sort by groupType.value
    @Query("SELECT DISTINCT g1 " +
            "FROM   Group g1 " +
            "JOIN   g1.groupRelationships g1r " +
            "JOIN   g1r.objectGroup.groupRoles gr2 " +
            "WHERE  (gr2.user = :user " +
            "OR     g1 IN (SELECT gr.group FROM User u JOIN u.groupRoles gr WHERE u = :user)) " +
            "AND    g1.visible = true " +
            "AND ((UPPER(g1.code) LIKE :filterText) " +
            "OR (UPPER(g1.name) LIKE :filterText)) ")
    Page<Group> findGroupAndChildGroupsByUser(@Param("filterText") String filterText,
                                                     @Param("user") User user, Pageable pageable);

    // get group and children
    @Query("SELECT DISTINCT g1 " +
            "FROM   Group g1 " +
            "JOIN   g1.groupRelationships g1r " +
            "JOIN   g1r.objectGroup.groupRoles gr2 " +
            "WHERE  (gr2.user = :user " +
            "OR     g1 IN (SELECT gr.group FROM User u JOIN u.groupRoles gr WHERE u = :user)) " +
            "AND    g1.visible = true " +
            "AND ((UPPER(g1.code) LIKE :filterText) " +
            "OR (UPPER(g1.name) LIKE :filterText)) " +
            "AND (g1.groupType.id IN (:groupTypes))")
    Page<Group> findGroupAndChildGroupsByUserAndGroupType(@Param("filterText") String filterText,
                                                     @Param("groupTypes") List<Long> groupTypes,
                                                     @Param("user") User user, Pageable pageable);

    @Query("SELECT gr.objectGroup " +
           "FROM   GroupRelationship gr " +
           "WHERE  gr.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.CHILD " +
           "AND    gr.sourceGroup = :group ")
    List<Group> findChildren(@Param("group") Group group);

    @Query("SELECT g " +
            "FROM Group g " +
            "WHERE g.id IN :ids")
    List<Group> findAllByIds(@Param("ids") List<Long> ids);

    Group findByCode(String code);

    Iterable<Group> findByName(String name);

    @Query("SELECT g " +
            "FROM Group g " +
            "WHERE g.visibleToJoin = true")
    List<Group> findAllVisibleToJoin();

    List<Group> findAll();

    @Query("SELECT g FROM Group g " +
            "JOIN g.groupFeatures gf " +
            "WHERE gf.feature = :feature ")
    List<Group> findByFeature(@Param("feature") Feature feature);
}
