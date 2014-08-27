package org.patientview.api.service;

import org.patientview.api.annotation.AuditTrail;
import org.patientview.config.exception.ResourceNotFoundException;
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

    List<Feature> getUserFeatures(Long userId) throws ResourceNotFoundException ;

    User getByUsername(String username);

    User getByEmail(String username);

    @AuditTrail(value = AuditActions.CREATE, objectType = User.class)
    User createUserWithPasswordEncryption(User user);

    @AuditTrail(value = AuditActions.CREATE, objectType = User.class)
    User createUserNoEncryption(User user);

    @AuditTrail(value = AuditActions.EDIT, objectType = User.class)
    User save(User user) throws ResourceNotFoundException;

    @AuditTrail(value = AuditActions.VIEW, objectType = User.class)
    User get(Long userId);

    List<User> getUsersByGroupsAndRoles(List<Long> groupIds,List<Long> roleIds);

    @AuditTrail(value = AuditActions.CHANGE_PASSWORD, objectType = User.class)
    User changePassword(final Long userId, final String password) throws ResourceNotFoundException ;

    @AuditTrail(value = AuditActions.CHANGE_PASSWORD, objectType = User.class)
    User resetPassword(Long userId, String password) throws ResourceNotFoundException ;

    Boolean sendVerificationEmail(Long userId);

    Boolean verify(Long userId, String verificationCode) throws ResourceNotFoundException;

    Identifier addIdentifier(Long userId, Identifier identifier) throws ResourceNotFoundException;

    void addFeature(Long userId, Long featureId);

    void deleteFeature(Long userId, Long featureId);

    void resetPasswordByUsernameAndEmail(String username, String email) throws ResourceNotFoundException;
}
