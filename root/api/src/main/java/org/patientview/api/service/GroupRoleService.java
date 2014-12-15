package org.patientview.api.service;

import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 12/12/2014
 *
 * Migration only
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupRoleService {
    List<GroupRole> findByUser(User user);
}
