package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.Credentials;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.AdminService;
import org.patientview.api.service.UserService;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.UserInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@RestController
public class UserController extends BaseController<UserController> {

    private final static Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Inject
    private UserService userService;

    @Inject
    private AdminService adminService;

    final private static int MINIMUM_PASSWORD_LENGTH = 7;

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<org.patientview.api.model.User> getUser(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(new org.patientview.api.model.User(userService.get(userId), null), HttpStatus.OK);

    }

    @RequestMapping(value = "/user/{userId}/group/{groupId}/role/{roleId}", method = RequestMethod.PUT)
    @ResponseBody
    public void addUserGroupRole(@PathVariable("userId") Long userId, @PathVariable("groupId") Long groupId,
            @PathVariable("roleId") Long roleId) throws ResourceNotFoundException {
        userService.addGroupRole(userId, groupId, roleId);
    }

    @RequestMapping(value = "/user/{userId}/group/{groupId}/role/{roleId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteUserGroupRole(@PathVariable("userId") Long userId,
          @PathVariable("groupId") Long groupId, @PathVariable("roleId") Long roleId) throws ResourceNotFoundException {
        userService.deleteGroupRole(userId, groupId, roleId);
    }

    // handle getting users from multiple groups and roles using query parameters
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.User>> getUsers(GetParameters getParameters)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(userService.getUsersByGroupsAndRoles(getParameters), HttpStatus.OK);
    }

    // TODO Sprint 2, required by migration
    @RequestMapping(value = "/user/username", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUserByUsername(@RequestParam("username") String username) {
        return new ResponseEntity<>(userService.getByUsername(username), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteUser(@PathVariable("userId") Long userId) throws ResourceNotFoundException {
        userService.delete(userId);
    }

    // TODO Sprint 3 split this into different methods as UI only needs transport user
    @RequestMapping(value = "/user", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> createUser(@RequestBody User user,
       @RequestParam(value = "encryptPassword", required = false) Boolean encryptPassword)
            throws ResourceNotFoundException {

        LOG.debug("Request has been received for userId : {}", user.getUsername());
        user.setCreator(userService.get(1L));

        if (encryptPassword != null && encryptPassword.equals(Boolean.FALSE)) {
            // Migration Only, are migrating passwords so create user with no password encryption
            try {
                user = userService.createUserNoEncryption(user);
            }
            catch (EntityExistsException eee) {
                User foundUser = userService.getByUsername(user.getUsername());
                if (foundUser != null) {
                    // found by username
                    return new ResponseEntity<>(foundUser, HttpStatus.CONFLICT);
                } else {
                    // found by email
                    return new ResponseEntity<>(userService.getByEmail(user.getEmail()), HttpStatus.CONFLICT);
                }
            }
        }

        if (user.getId() == null) {
            // creating new user in UI
            try {
                user = userService.createUserWithPasswordEncryption(user);
            } catch (EntityExistsException eee) {
                User foundUser = userService.getByUsername(user.getUsername());
                if (foundUser != null) {
                    // found by username
                    return new ResponseEntity<>(foundUser, HttpStatus.CONFLICT);
                } else {
                    // found by email
                    return new ResponseEntity<>(userService.getByEmail(user.getEmail()), HttpStatus.CONFLICT);
                }
            }
        }

        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/user", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public void updateUser(@RequestBody User user) throws ResourceNotFoundException {
        userService.save(user);
    }

    @RequestMapping(value = "/user/{userId}/features", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Feature>> getUserFeatures(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {

        LOG.debug("Request has been received for userId : {}", userId);
        return new ResponseEntity<>(userService.getUserFeatures(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/resetPassword", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> resetPassword(@PathVariable("userId") Long userId,
                                              @RequestBody Credentials credentials) throws ResourceNotFoundException {

        if (StringUtils.isEmpty(credentials.getPassword())) {
            LOG.debug("A password must be supplied");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        LOG.debug("Password reset requested for userId : {}", userId);
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

        LOG.debug("Password reset requested for userId : {}", userId);
        userService.changePassword(userId, credentials.getPassword());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/user/{userId}/sendVerificationEmail", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> sendVerificationEmail(@PathVariable("userId") Long userId) {
        LOG.debug("Email verification email requested for userId : {}", userId);
        return new ResponseEntity<>(userService.sendVerificationEmail(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/verify/{verificationCode}", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> verify(@PathVariable("userId") Long userId,
                                          @PathVariable("verificationCode") String verificationCode)
    throws ResourceNotFoundException {
        LOG.debug("User with userId : {} is verifying with code {}", userId, verificationCode);
        return new ResponseEntity<>(userService.verify(userId, verificationCode), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/role/{roleId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Role>> getUserByRoles(@PathVariable("roleId") Long roleId,
                                                     UriComponentsBuilder uriComponentsBuilder) {

        LOG.debug("Request has been received for userId : {}", roleId);

        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(roleId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<>(adminService.getAllRoles(), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/identifiers", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Identifier> addIdentifier(@PathVariable("userId") Long userId,
                                          @RequestBody Identifier identifier)
            throws ResourceNotFoundException, EntityExistsException {
        LOG.debug("User with userId : {} is verifying with code {}", userId, identifier);
        return new ResponseEntity<>(userService.addIdentifier(userId, identifier), HttpStatus.CREATED);
    }

    @RequestMapping(value = "/user/{userId}/features/{featureId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> addFeature(@PathVariable("userId") Long userId,
                                           @PathVariable("featureId") Long featureId) {
        userService.addFeature(userId, featureId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/features/{featureId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteFeature(@PathVariable("userId") Long userId,
                                              @PathVariable("featureId") Long featureId) {
        userService.deleteFeature(userId, featureId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/information", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> addInformation(@PathVariable("userId") Long userId,
                                               @RequestBody List<UserInformation> userInformation)
            throws ResourceNotFoundException {
        userService.addInformation(userId, userInformation);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @RequestMapping(value = "/user/{userId}/information", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<UserInformation>> getInformation(@PathVariable("userId") Long userId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(userService.getInformation(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/identifier/value/{identifierValue}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Identifier> getIdentifierByValue(@PathVariable("identifierValue") String identifierValue)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(userService.getIdentifierByValue(identifierValue), HttpStatus.OK);
    }
}
