package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.Credentials;
import org.patientview.api.service.MigrationService;
import org.patientview.api.service.UserMigrationService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.patientview.persistence.model.GetParameters;
import org.patientview.api.model.User;
import org.patientview.persistence.model.MigrationUser;
import org.patientview.persistence.model.UserInformation;
import org.patientview.persistence.model.enums.MigrationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@RestController
public class UserController extends BaseController<UserController> {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Inject
    private UserService userService;

    @Inject
    private UserMigrationService userMigrationService;

    @Inject
    private MigrationService migrationService;

    private static final int MINIMUM_PASSWORD_LENGTH = 7;

    // get User, secured in service
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<org.patientview.api.model.User> getUser(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(userService.getUser(userId), HttpStatus.OK);
    }

    // add group role to user
    @RequestMapping(value = "/user/{userId}/group/{groupId}/role/{roleId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addGroupRole(@PathVariable("userId") Long userId, @PathVariable("groupId") Long groupId,
            @PathVariable("roleId") Long roleId) throws ResourceNotFoundException, ResourceForbiddenException {
        userService.addGroupRole(userId, groupId, roleId);
    }

    // remove group role from user
    @RequestMapping(value = "/user/{userId}/group/{groupId}/role/{roleId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteGroupRole(@PathVariable("userId") Long userId, @PathVariable("groupId") Long groupId,
            @PathVariable("roleId") Long roleId) throws ResourceNotFoundException, ResourceForbiddenException {
        userService.deleteGroupRole(userId, groupId, roleId);
    }

    // remove all group roles for user (used when 'deleting' user with KEEP_ALL_DATA feature available on one of
    // their groups)
    @RequestMapping(value = "/user/{userId}/removeallgrouproles", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeAllGroupRoles(@PathVariable("userId") Long userId) throws ResourceNotFoundException {
        userService.removeAllGroupRoles(userId);
    }

    // handle getting users from multiple groups and roles using query parameters
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.User>> getUsers(GetParameters getParameters)
            throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(userService.getApiUsersByGroupsAndRoles(getParameters), HttpStatus.OK);
    }

    // required by migration
    @RequestMapping(value = "/user/username", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUserByUsername(@RequestParam("username") String username) {
        return new ResponseEntity<>(userService.getByUsername(username), HttpStatus.OK);
    }

    // permanently delete user (NOT just remove from all groups)
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteUser(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException {
        userService.delete(userId);
    }

    // Creating new user in UI
    @RequestMapping(value = "/user", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Long> createUser(@RequestBody org.patientview.persistence.model.User user)
            throws ResourceNotFoundException, ResourceForbiddenException {
        try {
            return new ResponseEntity<>(userService.createUserWithPasswordEncryption(user), HttpStatus.CREATED);
        } catch (EntityExistsException eee) {
            User foundUser = userService.getByUsername(user.getUsername());
            if (foundUser != null) {
                // found by username
                return new ResponseEntity<>(foundUser.getId(), HttpStatus.CONFLICT);
            } else {
                // found by email
                return new ResponseEntity<>(userService.getByEmail(user.getEmail()).getId(), HttpStatus.CONFLICT);
            }
        }
    }

    // Migration Only, are migrating passwords so create user with no password encryption
    @RequestMapping(value = "/migrate/user", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Long> migrateUser(@RequestBody MigrationUser migrationUser)
            throws ResourceNotFoundException, EntityExistsException, MigrationException {
        return new ResponseEntity<>(migrationService.migrateUser(migrationUser), HttpStatus.CREATED);
    }

    // Migration Only
    @RequestMapping(value = "/migrate/observations", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void migrateObservations(@RequestBody MigrationUser migrationUser)
            throws ResourceNotFoundException, EntityExistsException, MigrationException {
        migrationService.migrateObservations(migrationUser);
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

    @RequestMapping(value = "/user", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateUser(@RequestBody org.patientview.persistence.model.User user)
            throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {
        userService.save(user);
    }

    @RequestMapping(value = "/user/{userId}/settings", method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateOwnSettings(@RequestBody org.patientview.persistence.model.User user,
                                  @PathVariable("userId") Long userId)
            throws EntityExistsException, ResourceNotFoundException, ResourceForbiddenException {
        userService.updateOwnSettings(userId, user);
    }

    // reset password, done by users for other staff or patients
    @RequestMapping(value = "/user/{userId}/resetPassword", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> resetPassword(@PathVariable("userId") Long userId,
            @RequestBody Credentials credentials) throws ResourceNotFoundException, ResourceForbiddenException {

        if (StringUtils.isEmpty(credentials.getPassword())) {
            LOG.debug("A password must be supplied");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(userService.resetPassword(userId, credentials.getPassword()), HttpStatus.OK);
    }

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

    @RequestMapping(value = "/user/{userId}/sendVerificationEmail", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> sendVerificationEmail(@PathVariable("userId") Long userId)
        throws ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(userService.sendVerificationEmail(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/verify/{verificationCode}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> verify(@PathVariable("userId") Long userId,
        @PathVariable("verificationCode") String verificationCode)
            throws ResourceNotFoundException, VerificationException {
        return new ResponseEntity<>(userService.verify(userId, verificationCode), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/features/{featureId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addFeature(@PathVariable("userId") Long userId, @PathVariable("featureId") Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.addFeature(userId, featureId);
    }

    @RequestMapping(value = "/user/{userId}/features/{featureId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteFeature(@PathVariable("userId") Long userId, @PathVariable("featureId") Long featureId)
            throws ResourceNotFoundException, ResourceForbiddenException {
        userService.deleteFeature(userId, featureId);
    }

    @RequestMapping(value = "/user/{userId}/information", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void addInformation(@PathVariable("userId") Long userId,
                               @RequestBody List<UserInformation> userInformation) throws ResourceNotFoundException {
        userService.addInformation(userId, userInformation);
    }

    @RequestMapping(value = "/user/{userId}/information", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<UserInformation>> getInformation(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(userService.getInformation(userId), HttpStatus.OK);
    }

    // used when searching for existing patients
    @RequestMapping(value = "/user/identifier/{identifier}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUserByIdentifier(@PathVariable("identifier") String identifier)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(userService.getByIdentifierValue(identifier), HttpStatus.OK);
    }
}
