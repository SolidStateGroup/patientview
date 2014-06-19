package org.patientview.api.service;

import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UserService {

    List<Feature> getUserFeatures(Long userId);

    User getByUsername(String username);

    void deleteUser(Long userId);

    User getUser(Long userId);

    User createUser(User user);

    User saveUser(User user);

    List<Route> getUserRoutes(Long userId);

    List<User> getUserByGroupAndRole(Long groupId, Long roleId);

}
