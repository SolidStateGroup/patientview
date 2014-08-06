package org.patientview.api.service;

import org.patientview.api.annotation.AuditTrail;
import org.patientview.api.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UserService extends CrudService<User> {

    List<Feature> getUserFeatures(Long userId);

    User getByUsername(String username);

    User getByEmail(String username);

    /**
     * This persists the User map with GroupRoles and UserFeatures. The static
     * data objects are detached so have to be become managed again without updating the objects.
     *
     * @param user
     * @return
     */
    @AuditTrail(value = AuditActions.CREATE, objectType = User.class)
    User createUserWithPasswordEncryption(User user);

    @AuditTrail(value = AuditActions.CREATE, objectType = User.class)
    public User createUserNoEncryption(User user);

    @AuditTrail(value = AuditActions.EDIT, objectType = User.class)
    User save(User user) throws ResourceNotFoundException;

    List<User> getUserByGroupAndRole(Long groupId, Long roleId);

    /**
     * Get users based on a list of groups and role types
     * @param groupIds
     * @param roleIds
     * @return
     */
    List<User> getUsersByGroupsAndRoles(List<Long> groupIds,List<Long> roleIds);

    /**
     * This persists the User in the above method with a new password.
     *
     * @param user
     * @return
     */
    User createUserResetPassword(User user);

    @AuditTrail(value = AuditActions.CHANGE_PASSWORD, objectType = User.class)
    User updatePassword(Long userId, String password);

    public Boolean sendVerificationEmail(Long userId);

    public Boolean verify(Long userId, String verificationCode) throws ResourceNotFoundException;

    Identifier addIdentifier(Long userId, Identifier identifier) throws ResourceNotFoundException;

    void addFeature(Long userId, Long featureId);

    void deleteFeature(Long userId, Long featureId);
}
