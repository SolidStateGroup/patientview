package org.patientview.persistence.repository;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Lookup;
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
    public Page<Group> findAll(@Param("filterText") String filterText, Pageable pageable);

    @Query("SELECT g FROM Group g " +
            "WHERE ((UPPER(g.code) LIKE :filterText) " +
            "OR (UPPER(g.name) LIKE :filterText)) " +
            "AND (g.groupType.id IN (:groupTypes))")
    public Page<Group> findAllByGroupType(@Param("filterText") String filterText,
                                           @Param("groupTypes") List<Long> groupTypes,
                                           Pageable pageable);

    @Query("SELECT u " +
           "FROM   User u JOIN u.groupRoles gr " +
           "WHERE  gr.group.id = :groupId " +
           "AND    gr.role.roleType.value = :roleType")
    public Iterable<User> findGroupStaffByRole(@Param("groupId") Long groupId,
                                               @Param("roleType") String roleType);

    @Query("SELECT gr.group " +
           "FROM   User u " +
           "JOIN   u.groupRoles gr " +
           "WHERE  gr.role = :role " +
           "AND    u = :user")
    public Iterable<Group> findGroupByUserAndRole(@Param("user") User user,
                                                  @Param("role") Role role);

    @Query("SELECT gr.group " +
           "FROM   User u " +
           "JOIN   u.groupRoles gr " +
           "WHERE  u = :user " +
           "AND    gr.group.visible = true ")
    public Iterable<Group> findGroupByUser(@Param("user") User user);

    @Query("SELECT gr.group " +
           "FROM   User u " +
           "JOIN   u.groupRoles gr " +
           "WHERE  u = :user " +
           "AND    gr.group.visible = true " +
           "AND    gr.group.groupType.value <> 'SPECIALTY' " +
           "AND ((UPPER(gr.group.code) LIKE :filterText) " +
           "OR (UPPER(gr.group.name) LIKE :filterText)) ")
    public Page<Group> findGroupsByUserNoSpecialties(@Param("filterText") String filterText,
                                                     @Param("user") User user, Pageable pageable);

    @Query("SELECT gr.group " +
           "FROM   User u " +
           "JOIN   u.groupRoles gr " +
           "WHERE  u = :user " +
           "AND    gr.group.visible = true " +
           "AND    gr.group.groupType.value <> 'SPECIALTY' " +
           "AND ((UPPER(gr.group.code) LIKE :filterText) " +
           "OR (UPPER(gr.group.name) LIKE :filterText)) " +
           "AND (gr.group.groupType.id IN (:groupTypes))")
    public Page<Group> findGroupsByUserAndGroupTypeNoSpecialties(@Param("filterText") String filterText,
                                                     @Param("groupTypes") List<Long> groupTypes,
                                                     @Param("user") User user, Pageable pageable);

    // get group and children
    // TODO: this query and below need work to work in both postgresql 9.4b1 and 9.4b2
    // TODO: requires DISTINCT in b1 but not b2 but with DISTINCT fails to sort by groupType.value
    @Query("SELECT DISTINCT g1 " +
            "FROM   Group g1 " +
            "JOIN   g1.groupRelationships g1r " +
            "JOIN   g1r.objectGroup.groupRoles gr2 " +
            "LEFT  JOIN g1.groupRoles gr1 " +
            "WHERE  (gr2.user = :user " +
            "OR     gr1.user = :user) " +
            "AND    g1.visible = true " +
            "AND ((UPPER(g1.code) LIKE :filterText) " +
            "OR (UPPER(g1.name) LIKE :filterText)) ")// +
            //"AND  g1r.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.CHILD")
    public Page<Group> findGroupAndChildGroupsByUser(@Param("filterText") String filterText,
                                                     @Param("user") User user, Pageable pageable);
    // get group and children
    @Query("SELECT DISTINCT g1 " +
            "FROM   Group g1 " +
            "JOIN   g1.groupRelationships g1r " +
            "JOIN   g1r.objectGroup.groupRoles gr2 " +
            "LEFT  JOIN g1.groupRoles gr1 " +
            "WHERE  (gr2.user = :user " +
            "OR     gr1.user = :user) " +
            "AND    g1.visible = true " +
            "AND ((UPPER(g1.code) LIKE :filterText) " +
            "OR (UPPER(g1.name) LIKE :filterText)) " +
            "AND (g1.groupType.id IN (:groupTypes))")
    public Page<Group> findGroupAndChildGroupsByUserAndGroupType(@Param("filterText") String filterText,
                                                     @Param("groupTypes") List<Long> groupTypes,
                                                     @Param("user") User user, Pageable pageable);

    @Query("SELECT g " +
           "FROM   Group g " +
           "WHERE  g.groupType = :groupType")
    public Iterable<Group> findGroupByType(@Param("groupType") Lookup groupType);

    @Query("SELECT gr.objectGroup " +
           "FROM   GroupRelationship gr " +
           "WHERE  gr.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.CHILD " +
           "AND    gr.sourceGroup = :group ")
    public Iterable<Group> findChildren(@Param("group") Group group);

    public Group findByCode(String code);

    public Iterable<Group> findByName(String name);

}
