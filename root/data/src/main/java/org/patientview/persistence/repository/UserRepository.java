package org.patientview.persistence.repository;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    @Query("SELECT u FROM User u WHERE username = :username AND password = :password")
    User findByUsernameAndPassword(@Param("username") String username, @Param("password")  String password);

   // Iterable<User> findByGroup(Group group);

    @Modifying
    @Query("SELECT u FROM User u JOIN u.groupRoles gr WHERE gr.role = :role AND gr.group = :group")
    Iterable<User> findByGroupAndRole(@Param("group") Group group, @Param("role") Role role);
}
