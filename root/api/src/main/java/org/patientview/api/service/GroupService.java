package org.patientview.api.service;

import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 09/07/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupService {

    List<Group> findAll();

    List<Group> findGroupByUser(User user);

    List<Group> findGroupByType(Long lookupId);
}
