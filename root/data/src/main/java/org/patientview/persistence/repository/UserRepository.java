package org.patientview.persistence.repository;

import org.patientview.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
