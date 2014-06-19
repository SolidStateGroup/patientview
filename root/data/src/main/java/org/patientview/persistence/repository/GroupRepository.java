package org.patientview.persistence.repository;

import org.patientview.persistence.model.Group;
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
           "AND    gr.role.roleType.lookupType.type = :roleType")
    public Iterable<User> getGroupStaffByRole(@Param("groupId") Long groupId,
                                              @Param("roleType") String roleType);


    @Query("SELECT gr.group " +
           "FROM   User u " +
           "JOIN   u.groupRoles gr " +
           "WHERE  gr.role.id = :roleId " +
           "AND    u.id = :userId")
    public Iterable<Group> getGroupByUserAndRole(@Param("userId") Long userId,
                                                @Param("roleId") Long roleId);

}
