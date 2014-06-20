package org.patientview.api.service;

import org.patientview.persistence.model.Feature;
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

    /**
     * This persists the User map with GroupRoles and UserFeatures. The static
     * data objects are detached so have to be become managed again without updating the objects.
     *
     * @param user
     * @return
     */
    User createUser(User user);

    User saveUser(User user);

    List<User> getUserByGroupAndRole(Long groupId, Long roleId);

    /**
     * This persists the User in the above method with a new password.
     *
     * @param user
     * @return
     */
    User createUserResetPassword(User user);

}
