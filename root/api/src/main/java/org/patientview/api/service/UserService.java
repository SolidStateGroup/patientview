package org.patientview.api.service;

import org.patientview.api.annotation.AuditTrail;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.MigrationUser;
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

    @AuditTrail(value = AuditActions.CREATE, objectType = User.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Long createUserWithPasswordEncryption(User user)
        throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException;

    // used by migration
    @AuditTrail(value = AuditActions.CREATE, objectType = User.class)
    Long migrateUser(MigrationUser migrationUser)
            throws EntityExistsException, ResourceNotFoundException, MigrationException;

    @AuditTrail(value = AuditActions.EDIT, objectType = User.class)
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(User user) throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException;

    @AuditTrail(value = AuditActions.VIEW, objectType = User.class)
    User get(Long userId) throws ResourceNotFoundException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void delete(Long userId) throws ResourceNotFoundException, ResourceForbiddenException;

    @AuditTrail(value = AuditActions.VIEW, objectType = User.class)
    org.patientview.api.model.User getUser(Long userId) throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    GroupRole addGroupRole(Long userId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void deleteGroupRole(Long userId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void removeAllGroupRoles(Long userId) throws ResourceNotFoundException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN })
    Page<org.patientview.api.model.User> getUsersByGroupsAndRoles(GetParameters getParameters);

    Page<org.patientview.api.model.User> getUsersByGroupsRolesFeatures(GetParameters getParameters);

    @AuditTrail(value = AuditActions.CHANGE_PASSWORD, objectType = User.class)
    @UserOnly
    void changePassword(final Long userId, final String password) throws ResourceNotFoundException;

    @AuditTrail(value = AuditActions.CHANGE_PASSWORD, objectType = User.class)
    org.patientview.api.model.User resetPassword(Long userId, String password)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Boolean sendVerificationEmail(Long userId) throws ResourceNotFoundException, ResourceForbiddenException;

    Boolean verify(Long userId, String verificationCode) throws ResourceNotFoundException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void addFeature(Long userId, Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void deleteFeature(Long userId, Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    void resetPasswordByUsernameAndEmail(String username, String email) throws ResourceNotFoundException;

    @UserOnly
    void addInformation(Long userId, List<UserInformation> userInformation) throws ResourceNotFoundException;

    @UserOnly
    List<UserInformation> getInformation(Long userId) throws ResourceNotFoundException;
}
