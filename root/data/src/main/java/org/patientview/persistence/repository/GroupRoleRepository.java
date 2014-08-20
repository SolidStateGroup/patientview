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

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@Repository
@Transactional(propagation = Propagation.MANDATORY)
public interface GroupRoleRepository extends CrudRepository<GroupRole, Long> {

    @Modifying
    @Query("DELETE FROM GroupRole gr WHERE gr.user = :user")
    void deleteByUser(@Param("user") User user);

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

}
