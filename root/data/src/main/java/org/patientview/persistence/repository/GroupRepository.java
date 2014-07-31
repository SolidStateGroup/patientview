package org.patientview.persistence.repository;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface GroupRepository extends CrudRepository <Group, Long> {
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
           "AND    gr.group.visible = true")
    public Iterable<Group> findGroupByUser(@Param("user") User user);

    @Query("SELECT g " +
           "FROM   Group g " +
           "WHERE  g.groupType = :groupType")
    public Iterable<Group> findGroupByType(@Param("groupType") Lookup groupType);

    @Query("SELECT gr.objectGroup " +
           "FROM   GroupRelationship gr " +
           "WHERE  gr.relationshipType = org.patientview.persistence.model.enums.RelationshipTypes.CHILD " +
           "AND    gr.sourceGroup = :group ")
    public Iterable<Group> findChildren(@Param("group") Group group);

}
