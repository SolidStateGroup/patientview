package org.patientview.persistence.repository;

import org.patientview.persistence.model.GroupRole;
import org.springframework.data.repository.CrudRepository;
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
}
