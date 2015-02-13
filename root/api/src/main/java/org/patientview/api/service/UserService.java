package org.patientview.api.service;

import org.patientview.api.annotation.AuditTrail;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.GroupRole;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.mail.MailException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UserService {

    @RoleOnly
    void init();

    @RoleOnly
    Group getGenericGroup();

    org.patientview.api.model.User getByUsername(String username);

    User findByUsernameCaseInsensitive(String username);

    org.patientview.api.model.User getByEmail(String username);

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    org.patientview.api.model.User getByIdentifierValue(String identifier) throws ResourceNotFoundException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Long createUserWithPasswordEncryption(User user)
        throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(User user) throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException;

    @UserOnly
    void updateOwnSettings(Long userId, User user)
            throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException;

    User get(Long userId) throws ResourceNotFoundException;

    /**
     * Delete user
     * Patients are deleted permanently, staff members are marked as deleted (for audit purposes)
     * @param userId ID of user to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void delete(Long userId, boolean forceDelete) 
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    org.patientview.api.model.User getUser(Long userId) throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    GroupRole addGroupRole(Long userId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void deleteGroupRole(Long userId, Long groupId, Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void removeAllGroupRoles(Long userId) throws ResourceNotFoundException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN,
            RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN })
    Page<org.patientview.api.model.User> getApiUsersByGroupsAndRoles(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException;

    Page<User> getUsersByGroupsAndRolesNoFilter(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException;

    Page<User> getUsersByGroupsRolesFeatures(GetParameters getParameters) throws ResourceNotFoundException;

    @AuditTrail(value = AuditActions.PASSWORD_CHANGE, objectType = User.class)
    @UserOnly
    void changePassword(final Long userId, final String password) throws ResourceNotFoundException;

    // reset password, done by users for other staff or patients
    @AuditTrail(value = AuditActions.PASSWORD_RESET, objectType = User.class)
    org.patientview.api.model.User resetPassword(Long userId, String password)
            throws ResourceNotFoundException, ResourceForbiddenException, MessagingException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Boolean sendVerificationEmail(Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException, MailException, MessagingException;

    @AuditTrail(value = AuditActions.EMAIL_VERIFY, objectType = User.class)
    Boolean verify(Long userId, String verificationCode) throws ResourceNotFoundException, VerificationException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void addFeature(Long userId, Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void deleteFeature(Long userId, Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException;

    // Stage 1 of Forgotten Password, user knows username and email
    void resetPasswordByUsernameAndEmail(String username, String email)
            throws ResourceNotFoundException, MailException, MessagingException;

    @UserOnly
    void addInformation(Long userId, List<UserInformation> userInformation) throws ResourceNotFoundException;

    void addOtherUsersInformation(Long userId, List<UserInformation> userInformation) throws ResourceNotFoundException;

    @UserOnly
    List<UserInformation> getInformation(Long userId) throws ResourceNotFoundException;

    Long add(User user) throws EntityExistsException;

    boolean currentUserCanGetUser(User user);

    boolean currentUserCanSwitchToUser(User user);

    void deleteFhirLinks(Long userId);
    
    @UserOnly
    String addPicture(Long userId, MultipartFile file) throws ResourceInvalidException;

    byte[] getPicture(Long userId) throws ResourceNotFoundException, ResourceForbiddenException;
}
