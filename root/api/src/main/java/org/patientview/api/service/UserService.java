package org.patientview.api.service;

import org.patientview.api.annotation.AuditTrail;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.RoleName;
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
public interface UserService {

    org.patientview.api.model.User getByUsername(String username);

    org.patientview.api.model.User getByEmail(String username);

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    org.patientview.api.model.User getByIdentifierValue(String identifier) throws ResourceNotFoundException;

    @AuditTrail(value = AuditActions.CREATE_USER, objectType = User.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Long createUserWithPasswordEncryption(User user)
        throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException;

    @AuditTrail(value = AuditActions.EDIT_USER, objectType = User.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(User user) throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException;

    @AuditTrail(value = AuditActions.EDIT_USER, objectType = User.class)
    @UserOnly
    void updateOwnSettings(Long userId, User user) throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException;

    User get(Long userId) throws ResourceNotFoundException;

    @AuditTrail(value = AuditActions.DELETE_USER, objectType = User.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void delete(Long userId) throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    org.patientview.api.model.User getUser(Long userId) throws ResourceNotFoundException, ResourceForbiddenException;

    @AuditTrail(value = AuditActions.ADD_GROUP_ROLE, objectType = User.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    GroupRole addGroupRole(Long userId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException;

    @AuditTrail(value = AuditActions.REMOVE_GROUP_ROLE, objectType = User.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void deleteGroupRole(Long userId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @AuditTrail(value = AuditActions.REMOVE_GROUP_ROLES, objectType = User.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void removeAllGroupRoles(Long userId) throws ResourceNotFoundException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN })
    Page<org.patientview.api.model.User> getApiUsersByGroupsAndRoles(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException;

    Page<User> getUsersByGroupsAndRoles(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException;

    Page<User> getUsersByGroupsRolesFeatures(GetParameters getParameters) throws ResourceNotFoundException;

    @AuditTrail(value = AuditActions.CHANGE_PASSWORD, objectType = User.class)
    @UserOnly
    void changePassword(final Long userId, final String password) throws ResourceNotFoundException;

    @AuditTrail(value = AuditActions.RESET_PASSWORD, objectType = User.class)
    org.patientview.api.model.User resetPassword(Long userId, String password)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Boolean sendVerificationEmail(Long userId) throws ResourceNotFoundException, ResourceForbiddenException;

    @AuditTrail(value = AuditActions.VERIFY_EMAIL, objectType = User.class)
    Boolean verify(Long userId, String verificationCode) throws ResourceNotFoundException, VerificationException;

    @AuditTrail(value = AuditActions.ADD_FEATURE, objectType = User.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void addFeature(Long userId, Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @AuditTrail(value = AuditActions.REMOVE_FEATURE, objectType = User.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void deleteFeature(Long userId, Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    void resetPasswordByUsernameAndEmail(String username, String email) throws ResourceNotFoundException;

    @UserOnly
    void addInformation(Long userId, List<UserInformation> userInformation) throws ResourceNotFoundException;

    void addOtherUsersInformation(Long userId, List<UserInformation> userInformation) throws ResourceNotFoundException;

    @UserOnly
    List<UserInformation> getInformation(Long userId) throws ResourceNotFoundException;

    @AuditTrail(value = AuditActions.CREATE_USER, objectType = User.class)
    Long add(User user) throws EntityExistsException;
}
