package org.patientview.persistence.repository;

import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface GroupRoleRepository extends CrudRepository<GroupRole, Long> {

    @Modifying
    @Query("DELETE FROM GroupRole gr WHERE gr.user = :user")
    void removeAllGroupRoles(@Param("user") User user);

    @Query("SELECT   gr " +
            "FROM    GroupRole gr " +
            "JOIN    gr.user u " +
            "JOIN    gr.group g " +
            "JOIN    gr.role r " +
            "WHERE   u = :user " +
            "AND     g = :group " +
            "AND     r = :role ")
    public GroupRole findByUserGroupRole(@Param("user") User user, @Param("group") Group group,
                                         @Param("role") Role role);
    @Query("SELECT   gr " +
            "FROM    GroupRole gr " +
            "JOIN    gr.group g " +
            "WHERE   g = :group ")
    public List<GroupRole> findByGroup(@Param("group") Group group);

    @Query("SELECT   gr " +
            "FROM    GroupRole gr " +
            "JOIN    gr.user u " +
            "WHERE   u = :user ")
    public List<GroupRole> findByUser(@Param("user") User user);

    @Query("SELECT CASE WHEN (count(gr) > 0) THEN TRUE ELSE FALSE END " +
            "FROM    GroupRole gr " +
            "JOIN    gr.user u " +
            "JOIN    gr.group g " +
            "JOIN    gr.role r " +
            "WHERE   u.id = :userId " +
            "AND     g.id = :groupId " +
            "AND     r.id = :roleId ")
    public boolean userGroupRoleExists(@Param("userId") Long userId, @Param("groupId") Long groupId,
                                       @Param("roleId") Long roleId);
}
