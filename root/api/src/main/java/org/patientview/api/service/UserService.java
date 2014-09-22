package org.patientview.api.service;

import org.patientview.api.annotation.AuditTrail;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.AuditActions;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
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

    GroupRole addGroupRole(Long userId, Long groupId, Long roleId) throws EntityExistsException;

    void deleteGroupRole(Long userId, Long groupId, Long roleId) throws ResourceNotFoundException;

    Page<org.patientview.api.model.User> getUsersByGroupsAndRoles(GetParameters getParameters);

    Page<org.patientview.api.model.User> getUsersByGroupsRolesFeatures(GetParameters getParameters);

    @AuditTrail(value = AuditActions.CHANGE_PASSWORD, objectType = User.class)
    User changePassword(final Long userId, final String password) throws ResourceNotFoundException ;

    @AuditTrail(value = AuditActions.CHANGE_PASSWORD, objectType = User.class)
    User resetPassword(Long userId, String password) throws ResourceNotFoundException ;

    Boolean sendVerificationEmail(Long userId);

    Boolean verify(Long userId, String verificationCode) throws ResourceNotFoundException;

    Identifier addIdentifier(Long userId, Identifier identifier)
            throws ResourceNotFoundException, EntityExistsException;

    void addFeature(Long userId, Long featureId);

    void deleteFeature(Long userId, Long featureId);

    void resetPasswordByUsernameAndEmail(String username, String email) throws ResourceNotFoundException;

    void addInformation(Long userId, List<UserInformation> userInformation) throws ResourceNotFoundException;

    List<UserInformation> getInformation(Long userId) throws ResourceNotFoundException;

    Identifier getIdentifierByValue(String identifierValue) throws ResourceNotFoundException;
}
