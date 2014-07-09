package org.patientview.persistence.repository;

import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
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
public interface RoleRepository extends CrudRepository<Role, Long> {

    @Query("SELECT r " +
           "FROM Role r " +
           "WHERE r.level < (SELECT MAX(r.level) FROM User u JOIN u.groupRoles gr JOIN gr.role r WHERE u.id = :userId)")
    public List<Role> getValidRolesByUser(@Param("userId") Long userId);

    @Query("SELECT rol FROM Role rol WHERE rol.roleType = :roleType")
    public List<Role> getByType(@Param("roleType") Lookup roleType);

    @Query("SELECT gr.role FROM User u JOIN u.groupRoles gr WHERE u = :user")
    public List<Role> getByUser(@Param("user") User user);
}
