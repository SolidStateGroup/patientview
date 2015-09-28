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
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * User service, for managing Users, User information, resetting passwords etc.
 *
 * Created by james@solidstategroup.com
 * Created on 19/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UserService {

    /**
     * Create a new User.
     * @param user User to create
     * @return Lond ID of newly created User
     * @throws EntityExistsException
     */
    Long add(User user) throws EntityExistsException;

    /**
     * Add a Feature to a User.
     * @param userId ID of User to add Feature to
     * @param featureId ID of Feature to add to User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void addFeature(Long userId, Long featureId)
    throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Add a Group and associated Role to a User.
     * @param userId ID of User to add Group and Role to
     * @param groupId ID of Group user is added to
     * @param roleId ID of Role that User joins Group with
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    GroupRole addGroupRole(Long userId, Long groupId, Long roleId)
    throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException;

    /**
     * Add UserInformation to a User, used when a patient adds More About Me etc.
     * @param userId ID of User to add UserInformation to
     * @param userInformation UserInformation to add to a User
     * @throws ResourceNotFoundException
     */
    @UserOnly
    void addInformation(Long userId, List<UserInformation> userInformation) throws ResourceNotFoundException;

    // used by migration
    void addOtherUsersInformation(Long userId, List<UserInformation> userInformation) throws ResourceNotFoundException;

    /**
     * Change the picture associated with a User account.
     * @param userId ID of User to change picture
     * @param file MultipartFile object containing picture information
     * @return String containing picture information, required by IE8 in response to uploading a file
     * @throws ResourceInvalidException
     */
    @UserOnly
    String addPicture(Long userId, MultipartFile file) throws ResourceInvalidException;

    /**
     * Change the picture associated with a User account. (base64)
     * @param userId ID of User to change picture
     * @param base64 Base64 string of image
     * @return String containing picture information, required by IE8 in response to uploading a file
     * @throws ResourceInvalidException
     */
    @UserOnly
    void addPicture(Long userId, String base64);

    /**
     * Used when a User changes their own password.
     * @param userId ID of User to change password
     * @param password New password
     * @return ResponseEntity, typically HTTP OK unless password does not match security requirements
     * @throws ResourceNotFoundException
     */
    @AuditTrail(value = AuditActions.PASSWORD_CHANGE, objectType = User.class)
    @UserOnly
    void changePassword(final Long userId, final String password) throws ResourceNotFoundException;

    /**
     * Create a new User, encrypting their password.
     * @param user User object containing all required information
     * @return ID of newly created User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Long createUserWithPasswordEncryption(User user)
            throws ResourceNotFoundException, ResourceForbiddenException, EntityExistsException;

    /**
     * Check if current User has the permissions to switch to another User.
     * @param user User to switch to
     * @return True if current User can switch to the other User
     */
    boolean currentUserCanSwitchToUser(User user);

    /**
     * Delete user. Patients are deleted permanently, staff members are marked as deleted (for audit purposes).
     * @param userId ID of user to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void delete(Long userId, boolean forceDelete)
    throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Remove a Feature from a User.
     * @param userId User to remove Feature from
     * @param featureId Feature to remove from User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void deleteFeature(Long userId, Long featureId)
    throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Delete all FhirLinks belonging to a User, used during deletion of a patient and in migration.
     * @param userId ID of User to delete FhirLinks from
     */
    void deleteFhirLinks(Long userId);

    /**
     * Remove a Group and associated Role from a User.
     * @param userId ID of User to remove Group and associated Role from
     * @param groupId ID of Group to remove User from
     * @param roleId ID of Role within Group to remove User from
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void deleteGroupRole(Long userId, Long groupId, Long roleId)
        throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Delete the picture associated with a User.
     * @param userId ID of User to delete picture from
     * @throws ResourceNotFoundException
     */
    @UserOnly
    void deletePicture(Long userId) throws ResourceNotFoundException;

    /**
     * Find a User by their username (case insensitive).
     * @param username String username of User to find
     * @return User object
     */
    User findByUsernameCaseInsensitive(String username);

    /**
     * Get a User.
     * @param userId ID of User to retrieve
     * @return User object
     * @throws ResourceNotFoundException
     */
    User get(Long userId) throws ResourceNotFoundException;

    /**
     * Get Users from multiple Groups, Roles based on GetParameters to filter and support pagination.
     * @param getParameters GetParameter object containing preferences for search, filter, pagination
     * @return Page of User objects
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN,
            RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN })
    Page<org.patientview.api.model.User> getApiUsersByGroupsAndRoles(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a User by email, used when searching for existing staff and creating new Users.
     * @param email String email used to search for Users
     * @return User object
     */
    org.patientview.api.model.User getByEmail(String email);

    /**
     * Get a User by Identifier value, used when searching for existing patients.
     * @param identifier Identifier value used to search for Users
     * @return User object
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    org.patientview.api.model.User getByIdentifierValue(String identifier) throws ResourceNotFoundException;

    /**
     * Get a User by username, used when searching for existing staff/patients and creating Users.
     * @param username String username used to search for Users
     * @return User object
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    org.patientview.api.model.User getByUsername(String username);

    /**
     * Get UserInformation (About Me etc) associated with a User.
     * @param userId ID of User to retrieve UserInformation for
     * @return List of UserInformation
     * @throws ResourceNotFoundException
     */
    @UserOnly
    List<UserInformation> getInformation(Long userId) throws ResourceNotFoundException;

    /**
     * Get a User.
     * @param userId ID of User to retrieve
     * @return User object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    org.patientview.api.model.User getUser(Long userId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Page of Users based on a list of groups and roles (only used by conversation service now).
     * @param getParameters GetParameter object containing preferences for pagination
     * @return Page of User objects
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    Page<User> getUsersByGroupsAndRolesNoFilter(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Get a Page of Users based on a list of groups and roles (only used by conversation service now).
     * @param getParameters GetParameter object containing preferences for pagination, filters
     * @return Page of User objects
     * @throws ResourceNotFoundException
     */
    Page<User> getUsersByGroupsRolesFeatures(GetParameters getParameters) throws ResourceNotFoundException;

    // used by migration
    @RoleOnly
    Group getGenericGroup();

    /**
     * Get a User's picture, returned as byte[] to allow direct viewing in browser when set as img source.
     * @param userId ID of User to retrieve picture for
     * @return byte[] binary picture data
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    byte[] getPicture(Long userId) throws ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Set up service with generic Group and member Role.
     */
    @RoleOnly
    void init();

    /**
     * Remove all Group membership and associated Roles for a User, used when 'deleting' a User when the KEEP_ALL_DATA
     * Feature is available on one of their groups. Note: consider refactoring to manage this within the service.
     * @param userId ID of User to remove all Groups and associated Roles from
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void removeAllGroupRoles(Long userId) throws ResourceNotFoundException;

    /**
     * Update a User.
     * @param user User to update
     * @throws EntityExistsException
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    void save(User user) throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Reset a User's password, done by Users for other staff or patients.
     * @param userId ID of User to reset password for
     * @param password New password
     * @return User, newly updated (note: consider only returning HTTP OK)
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws MessagingException
     */
    @AuditTrail(value = AuditActions.PASSWORD_RESET, objectType = User.class)
    org.patientview.api.model.User resetPassword(Long userId, String password)
            throws ResourceNotFoundException, ResourceForbiddenException, MessagingException;

    /**
     * Method called by a user who has forgotten their own password. User's put in their username and email address and
     * if they match receive an email with login details. User's must change their password on next login. This can be
     * considered step 1 of the forgotten password process, where step 2 is used if they do not know their username or
     * email.
     * @param username String username of User to reset password for
     * @param email String email of User to reset password for
     * @throws ResourceNotFoundException
     * @throws MailException
     * @throws MessagingException
     */
    void resetPasswordByUsernameAndEmail(String username, String email)
            throws ResourceNotFoundException, MailException, MessagingException;

    /**
     * Send verification email to a User in order to validate their email address is correct.
     * @param userId ID of User to send verification email to
     * @return True if sent successfully, false if not
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws MailException
     * @throws MessagingException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Boolean sendVerificationEmail(Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException, MailException, MessagingException;

    /**
     * Used when a User changes their own settings on the account page.
     * @param user User object containing updated User properties
     * @param userId ID of User to update
     * @throws EntityExistsException
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @UserOnly
    void updateOwnSettings(Long userId, User user)
            throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException;

    /**
     * Used when searching for existing Users in Create New Staff/Patient in UI, simple check to see if username is
     * already in use.
     * @param username String username to check if User already exists
     * @return True or false if username belongs to User that already exists
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN,
            RoleName.STAFF_ADMIN, RoleName.DISEASE_GROUP_ADMIN })
    boolean usernameExists(String username);

    /**
     * Verify a User's email address, link with verification code sent in the verification email.
     * @param userId ID of User to verify email
     * @param verificationCode String verification code used to verify email
     * @return True or false if User has correctly verified email
     * @throws ResourceNotFoundException
     * @throws VerificationException
     */
    @AuditTrail(value = AuditActions.EMAIL_VERIFY, objectType = User.class)
    Boolean verify(Long userId, String verificationCode) throws ResourceNotFoundException, VerificationException;

    String generateSalt() throws NoSuchAlgorithmException;
}
