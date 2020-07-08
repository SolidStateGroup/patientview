package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.Credentials;
import org.patientview.api.model.SecretWordInput;
import org.patientview.api.model.User;
import org.patientview.api.service.MigrationService;
import org.patientview.api.service.UserMigrationService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.persistence.EntityExistsException;
import java.util.List;
import java.util.Map;

/**
 * RESTful interface for managing and retrieving User data.
 *
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@RestController
@ExcludeFromApiDoc
public class UserController extends BaseController<UserController> {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Inject
    private UserService userService;

    @Inject
    private UserMigrationService userMigrationService;

    @Inject
    private MigrationService migrationService;

    private static final int MINIMUM_PASSWORD_LENGTH = 7;

    /**
     * Add a Feature to a User.
     * @param userId ID of User to add Feature to
     * @param featureId ID of Feature to add to User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/features/{featureId}", method = RequestMethod.POST)
    @ResponseBody
    public void addFeature(@PathVariable("userId") Long userId, @PathVariable("featureId") Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.addFeature(userId, featureId);
    }

    /**
     * Add a Group and associated Role to a User.
     * @param userId ID of User to add Group and Role to
     * @param groupId ID of Group user is added to
     * @param roleId ID of Role that User joins Group with
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/group/{groupId}/role/{roleId}", method = RequestMethod.POST)
    @ResponseBody
    public void addGroupRole(@PathVariable("userId") Long userId, @PathVariable("groupId") Long groupId,
                             @PathVariable("roleId") Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.addGroupRole(userId, groupId, roleId);
    }

    /**
     * Add UserInformation to a User, used when a patient adds More About Me etc.
     * @param userId ID of User to add UserInformation to
     * @param userInformation UserInformation to add to a User
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/information", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void addInformation(@PathVariable("userId") Long userId,
                               @RequestBody List<UserInformation> userInformation) throws ResourceNotFoundException {
        userService.addInformation(userId, userInformation);
    }

    /**
     * Used when a User changes their own password.
     * @param userId ID of User to change password
     * @param credentials Credentials object containing username and new password
     * @return ResponseEntity, typically HTTP OK unless password does not match security requirements
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/changePassword", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> changePassword(@PathVariable("userId") Long userId,
                                               @RequestBody Credentials credentials) throws ResourceNotFoundException {
        if (StringUtils.isEmpty(credentials.getPassword())) {
            LOG.debug("A password must be supplied");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (credentials.getPassword().length() < MINIMUM_PASSWORD_LENGTH) {
            LOG.debug("Password is not long enough");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        userService.changePassword(userId, credentials.getPassword());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Change the picture associated with a User account.
     * @param userId ID of User to change picture
     * @param file MultipartFile object containing picture information
     * @return String containing picture information, required by IE8 in response to uploading a file
     * @throws ResourceInvalidException
     */
    @RequestMapping(value = "/user/{userId}/picture", method = RequestMethod.POST,
            produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public ResponseEntity<String> changePicture(@PathVariable("userId") Long userId,
                                                @RequestParam("file") MultipartFile file)
            throws ResourceInvalidException, ResourceNotFoundException {
        return new ResponseEntity<>(userService.addPicture(userId, file), HttpStatus.OK);
    }

    /**
     * Change the picture associated with a User account.
     * @param userId ID of User to change picture
     * @param base64 base64 string containing picture information
     * @return String containing picture information, required by IE8 in response to uploading a file
     * @throws ResourceInvalidException
     */
    @RequestMapping(value = "/user/{userId}/picturebase64", method = RequestMethod.POST)
    @ResponseBody
    public void changePicture(@PathVariable("userId") Long userId, @RequestBody String base64)
            throws ResourceInvalidException, ResourceNotFoundException {
        userService.addPicture(userId, base64);
    }

    /**
     * Change the secret word for the user
     *
     * @param userId          a user id to change secret word for
     * @param secretWordInput a String pair containing secret word
     * @param salt            whether to include salt in response
     * @return newly generated salt a null if include salt param is false
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/changeSecretWord", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> changeSecretWord(@PathVariable("userId") Long userId,
                                                   @RequestBody SecretWordInput secretWordInput,
                                                   @RequestParam(value = "salt", required = false) String salt)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(
                userService.changeSecretWord(userId, secretWordInput, StringUtils.isNotBlank(salt)),
                HttpStatus.OK);
    }

    /**
     * Check if secret word has changed for the user.
     *
     * @param userId user id to validate secret word for
     * @param salt   an original salt value
     * @return if current secret word has been changed for the user, false otherwise
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/checkSecretWord/{salt}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Boolean> checkSecretWord(@PathVariable("userId") Long userId,
                                                   @PathVariable(value = "salt") String salt)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(userService.isSecretWordChanged(userId, salt), HttpStatus.OK);
    }

    /**
     * Create a new User.
     * @param user User object containing all required information
     * @return Long ID of newly created User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws VerificationException
     * @throws FhirResourceException
     * @throws EntityExistsException
     */
    @RequestMapping(value = "/user", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Long> createUser(@RequestBody org.patientview.persistence.model.User user)
            throws ResourceNotFoundException, ResourceForbiddenException, VerificationException, FhirResourceException,
            EntityExistsException {
        return new ResponseEntity<>(userService.createUserWithPasswordEncryption(user), HttpStatus.CREATED);
    }

    /**
     * Remove a Feature from a User.
     * @param userId User to remove Feature from
     * @param featureId Feature to remove from User
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/features/{featureId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteFeature(@PathVariable("userId") Long userId, @PathVariable("featureId") Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.deleteFeature(userId, featureId);
    }

    /**
     * Remove a Group and associated Role from a User.
     * @param userId ID of User to remove Group and associated Role from
     * @param groupId ID of Group to remove User from
     * @param roleId ID of Role within Group to remove User from
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/group/{groupId}/role/{roleId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteGroupRole(@PathVariable("userId") Long userId, @PathVariable("groupId") Long groupId,
                                @PathVariable("roleId") Long roleId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.deleteGroupRole(userId, groupId, roleId);
    }

    /**
     * Delete the picture associated with a User.
     * @param userId ID of User to delete picture from
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/picture", method = RequestMethod.DELETE)
    @ResponseBody
    public void deletePicture(@PathVariable("userId") Long userId) throws ResourceNotFoundException {
        userService.deletePicture(userId);
    }

    /**
     * Delete a User, currently patient Users are deleted permanently, staff are marked as deleted.
     * @param userId ID of User to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws FhirResourceException
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteUser(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        userService.delete(userId, false);
    }

    // Migration Only, used to get list of UserMigration migration status objects by status
    @RequestMapping(value = "/usermigration/{migrationstatus}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Long>> getPatientview1IdsByStatus(
            @PathVariable("migrationstatus") String migrationStatus)
            throws ResourceNotFoundException, EntityExistsException, MigrationException {
        return new ResponseEntity<>(userMigrationService.getPatientview1IdsByStatus(
                MigrationStatus.valueOf(migrationStatus)), HttpStatus.OK);
    }

    /**
     * Get UserInformation (About Me etc) associated with a User.
     * @param userId ID of User to retrieve UserInformation for
     * @return List of UserInformation
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/information", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<UserInformation>> getInformation(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(userService.getInformation(userId), HttpStatus.OK);
    }

    /**
     * Get a User's picture, returned as byte[] to allow direct viewing in browser when set as img source.
     * @param userId ID of User to retrieve picture for
     * @return byte[] binary picture data
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/picture", method = RequestMethod.GET, produces = MediaType.IMAGE_JPEG_VALUE)
    public HttpEntity<byte[]> getPicture(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        byte[] picture = userService.getPicture(userId);
        if (picture != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); //or what ever type it is
            headers.setContentLength(picture.length);
            return new HttpEntity<>(picture, headers);
        } else {
            return new HttpEntity<>(null, null);
        }
    }

    /**
     * Get a User.
     * @param userId ID of User to retrieve
     * @return User object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<org.patientview.api.model.User> getUser(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(userService.getUser(userId), HttpStatus.OK);
    }

    /**
     * Get a User by email, used when searching for existing staff.
     * @param email String email used to search for Users
     * @return User object
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/email/{email}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUserByEmail(@PathVariable("email") String email)
            throws ResourceNotFoundException {
        User user = userService.getByEmail(email.replace("[DOT]", "."));
        if (user == null) {
            throw new ResourceNotFoundException();
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    /**
     * Get a User by email, used when searching for existing staff.
     * Email passed as request parameter,rather then path variable, as above seemed
     * to have some issues.
     *
     * @param email String email used to search for Users
     * @return User object
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/email", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUserByEmailParam(@RequestParam("email") String email)
            throws ResourceNotFoundException {
        User user = userService.getByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException();
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    /**
     * Get a User by Identifier value, used when searching for existing patients.
     * @param identifier Identifier value used to search for Users
     * @return User object
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/identifier/{identifier}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUserByIdentifier(@PathVariable("identifier") String identifier)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(userService.getByIdentifierValue(identifier), HttpStatus.OK);
    }

    /**
     * Get a User by username, used when searching for existing staff/patients.
     * @param username String username used to search for Users
     * @return User object
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/username/{username}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUserByUsername(@PathVariable("username") String username)
            throws ResourceNotFoundException {
        User user = userService.getByUsername(username.replace("[DOT]", "."));
        if (user == null) {
            throw new ResourceNotFoundException();
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    // required by migration
    @RequestMapping(value = "/user/username", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUserByUsernameMigration(@RequestParam("username") String username) {
        return new ResponseEntity<>(userService.getByUsername(username), HttpStatus.OK);
    }

    /**
     * Get Users from multiple Groups, Roles based on GetParameters to filter and support pagination.
     * @param getParameters GetParameter object containing preferences for search, filter, pagination
     * @return Page of User objects
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.User>> getUsers(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(userService.getApiUsersByGroupsAndRoles(getParameters), HttpStatus.OK);
    }

    /**
     * Hide secret word notification
     * @param userId Id of User to hide secret word notification for
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/hideSecretWordNotification", method = RequestMethod.POST)
    @ResponseBody
    public void hideSecretWordNotification(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.hideSecretWordNotification(userId);
    }

    /**
     * List all duplicate GroupRole by iterating through groups and finding duplicates per group.
     */
    @RequestMapping(value = "/user/admin/listduplicategrouproles", method = RequestMethod.GET)
    public String listDuplicateGroupRoles() {
        return userService.listDuplicateGroupRoles();
    }

    // Migration Only, are migrating passwords so create user with no password encryption
    @RequestMapping(value = "/migrate/user", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Long> migrateUser(@RequestBody MigrationUser migrationUser)
            throws ResourceNotFoundException, EntityExistsException, ResourceForbiddenException,
            MigrationException, FhirResourceException {
        if (migrationUser.isPartialMigration()) {
            return new ResponseEntity<>(migrationService.migrateUserExisting(migrationUser), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(migrationService.migrateUser(migrationUser), HttpStatus.CREATED);
        }
    }

    /**
     * Move Users between groups, with optional check to remove/add parent group if a member of only one group
     * (originally used for RPV-651: SGC04 to SGC05)
     * @param groupFromId ID of Group to move users from
     * @param groupToId ID of Group to move users to
     * @param roleId ID of User's Role (to allow only moving patients or staff)
     * @param checkParentGroup boolean to remove/add parent group if only a member of one group
     * @throws ResourceForbiddenException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/users/movegroup/{groupFromId}/{groupToId}/{roleId}/{checkParentGroup}",
            method = RequestMethod.POST)
    @ResponseBody
    public void moveUsersGroup(@PathVariable("groupFromId") Long groupFromId, @PathVariable("groupToId") Long groupToId,
            @PathVariable("roleId") Long roleId, @PathVariable("checkParentGroup") boolean checkParentGroup)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.moveUsersGroup(groupFromId, groupToId, roleId, checkParentGroup);
    }

    /**
     * Remove all Group membership and associated Roles for a User, used when 'deleting' a User when the KEEP_ALL_DATA
     * Feature is available on one of their groups. Note: consider refactoring to manage this within the service.
     * @param userId ID of User to remove all Groups and associated Roles from
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/removeallgrouproles", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeAllGroupRoles(@PathVariable("userId") Long userId) throws ResourceNotFoundException {
        userService.removeAllGroupRoles(userId);
    }

    /**
     * Remove User's secret word.
     * @param userId Long ID of User to remove secret word for
     */
    @RequestMapping(value = "/user/{userId}/secretword", method = RequestMethod.DELETE)
    public void removeSecretWord(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.removeSecretWord(userId);
    }

    /**
     * Reset a User's password, done by Users for other staff or patients.
     * @param userId ID of User to reset password for
     * @param credentials Credentials object containing username and new password
     * @return User, newly updated (note: consider only returning HTTP OK)
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws MessagingException
     */
    @RequestMapping(value = "/user/{userId}/resetPassword", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> resetPassword(@PathVariable("userId") Long userId,
                                              @RequestBody Credentials credentials)
            throws ResourceNotFoundException, ResourceForbiddenException, MessagingException {

        if (StringUtils.isEmpty(credentials.getPassword())) {
            LOG.debug("A password must be supplied");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(userService.resetPassword(userId, credentials.getPassword()), HttpStatus.OK);
    }

    /**
     * Send verification email to a User in order to validate their email address is correct.
     * @param userId ID of User to send verification email to
     * @return True if sent successfully, false if not
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     * @throws MailException
     * @throws MessagingException
     */
    @RequestMapping(value = "/user/{userId}/sendVerificationEmail", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> sendVerificationEmail(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException, MailException, MessagingException {
        return new ResponseEntity<>(userService.sendVerificationEmail(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/undelete", method = RequestMethod.POST)
    @ResponseBody
    public void undeleteUser(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.undelete(userId);
    }

    /**
     * Used when a User changes their own settings on the account page.
     * @param user User object containing updated User properties
     * @param userId ID of User to update
     * @throws EntityExistsException
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/settings", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateOwnSettings(@RequestBody org.patientview.persistence.model.User user,
                                  @PathVariable("userId") Long userId)
            throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {
        userService.updateOwnSettings(userId, user);
    }

    /**
     * Update a User.
     * @param user User to update
     * @throws EntityExistsException
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateUser(@RequestBody org.patientview.persistence.model.User user)
            throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {
        userService.save(user);
    }

    /**
     * Used when searching for existing Users in Create New Staff/Patient in UI, simple check to see if username is
     * already in use.
     * @param username String username to check if User already exists
     * @return True or false if username belongs to User that already exists
     */
    @RequestMapping(value = "/user/usernameexists/{username:.+}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> usernameExists(@PathVariable("username") String username) {
        return new ResponseEntity<>(userService.usernameExists(username), HttpStatus.OK);
    }

    /**
     * Verify a User's email address, link with verification code sent in the verification email.
     * @param userId ID of User to verify email
     * @param verificationCode String verification code used to verify email
     * @return True or false if User has correctly verified email
     * @throws ResourceNotFoundException
     * @throws VerificationException
     */
    @RequestMapping(value = "/user/{userId}/verify/{verificationCode}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> verify(@PathVariable("userId") Long userId,
                                          @PathVariable("verificationCode") String verificationCode)
            throws ResourceNotFoundException, VerificationException {
        return new ResponseEntity<>(userService.verify(userId, verificationCode), HttpStatus.OK);
    }


    /**
     * Creates or regenerates new api key for patient
     *
     * @param userId
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/generateApiKey", method = RequestMethod.POST)
    @ResponseBody
    public void generateApiKey(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.generateApiKey(userId);
    }


    /**
     * Mobile endpoint to get stats for patient user such as unreadMessages, medicines, letters etc.
     *
     * Should extend this if more stats are needed.
     *
     * @param userId
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RequestMapping(value = "/user/{userId}/stats", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getUserStats(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, FhirResourceException {
        return new ResponseEntity<>(userService.getUserStats(userId), HttpStatus.OK);
    }
}
