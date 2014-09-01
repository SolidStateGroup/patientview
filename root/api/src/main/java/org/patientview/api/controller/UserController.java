package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.controller.model.Credentials;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.AdminService;
import org.patientview.api.service.GroupService;
import org.patientview.api.service.UserService;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Identifier;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.User;
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
    private GroupService groupService;

    @Inject
    private AdminService adminService;

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUser(@PathVariable("userId") Long userId) throws ResourceNotFoundException {
        return new ResponseEntity<>(userService.get(userId), HttpStatus.OK);

    }

    @RequestMapping(value = "/user/{userId}/group/{groupId}/role/{roleId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Void> addUserGroupRole(@PathVariable("userId") Long userId,
                                                      @PathVariable("groupId") Long groupId,
                                                      @PathVariable("roleId") Long roleId) {
        groupService.addGroupRole(userId, groupId, roleId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/group/{groupId}/role/{roleId}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteUserGroupRole(@PathVariable("userId") Long userId,
                                                      @PathVariable("groupId") Long groupId,
                                                      @PathVariable("roleId") Long roleId) {
        groupService.deleteGroupRole(userId, groupId, roleId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //TODO Sprint 2
    @RequestMapping(value = "/user/username", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUserByUsername(@RequestParam("username") String username) {
        return new ResponseEntity<>(userService.getByUsername(username), HttpStatus.OK);
    }

    // handle getting users from multiple groups and roles using query parameters
    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<org.patientview.api.model.User>> getUsers(GetParameters getParameters)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(userService.getUsersByGroupsAndRoles(getParameters), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public  ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId) throws ResourceNotFoundException {
        userService.delete(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // TODO Sprint 3 split this into different methods
    @RequestMapping(value = "/user", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> createUser(@RequestBody User user,
                               @RequestParam(value = "encryptPassword", required = false) Boolean encryptPassword,
                               UriComponentsBuilder uriComponentsBuilder) throws ResourceNotFoundException {

        LOG.debug("Request has been received for userId : {}", user.getUsername());
        user.setCreator(userService.get(1L));
        // Migration Only

        if (encryptPassword != null && encryptPassword.equals(Boolean.FALSE)) {
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
            try {
                user = userService.createUserWithPasswordEncryption(user);
            } catch (EntityExistsException eee) {
                return new ResponseEntity<>(userService.getByUsername(user.getUsername()), HttpStatus.CONFLICT);
            }
        }

        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(user.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(user, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/user", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> updateUser(@RequestBody User user, UriComponentsBuilder uriComponentsBuilder) {

        LOG.debug("Request has been received for userId : {}", user.getUsername());

        try {
            user = userService.save(user);
        } catch (ResourceNotFoundException rnf) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(user.getId());
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @RequestMapping(value = "/user/{userId}/features", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Feature>> getUserFeatures(@PathVariable("userId") Long userId,
                                                         UriComponentsBuilder uriComponentsBuilder)
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
    public ResponseEntity<User> changePassword(@PathVariable("userId") Long userId,
                                              @RequestBody Credentials credentials) throws ResourceNotFoundException {

        if (StringUtils.isEmpty(credentials.getPassword())) {
            LOG.debug("A password must be supplied");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        LOG.debug("Password reset requested for userId : {}", userId);
        return new ResponseEntity<>(userService.changePassword(userId, credentials.getPassword()), HttpStatus.OK);
    }


    @RequestMapping(value = "/user/{userId}/sendVerificationEmail", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Boolean> sendVerificationEmail(@PathVariable("userId") Long userId) {
        LOG.debug("Email verification email requested for userId : {}", userId);
        return new ResponseEntity<Boolean>(userService.sendVerificationEmail(userId), HttpStatus.OK);
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
            throws ResourceNotFoundException {
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

}
