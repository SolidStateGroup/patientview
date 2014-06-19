package org.patientview.api.controller;

import org.patientview.api.service.AdminService;
import org.patientview.api.service.UserService;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.Route;
import org.patientview.persistence.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@RestController
public class UserController extends BaseController {

    private final static Logger LOG = LoggerFactory.getLogger(GroupController.class);

    @Inject
    private UserService userService;

    @Inject
    private AdminService adminService;

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUser(@PathVariable("userId") Long userId) {
        return new ResponseEntity<User>(userService.getUser(userId), HttpStatus.OK);

    }

    @RequestMapping(value = "/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> getUserByUsername(@RequestParam("username") String username) {
        return new ResponseEntity<User>(userService.getByUsername(username), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public  ResponseEntity<Void> deleteUser(@PathVariable("userId") Long userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }


    @RequestMapping(value = "/user", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<User> createUser(@RequestBody User user, UriComponentsBuilder uriComponentsBuilder) {

        LOG.debug("Request has been received for userId : {}", user.getUsername());
        user.setCreator(userService.getUser(1L));

        user = userService.createUser(user);

        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(user.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<User>(user, headers, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/user", method = RequestMethod.PUT,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> updateUser(@RequestBody User user, UriComponentsBuilder uriComponentsBuilder) {

        LOG.debug("Request has been received for userId : {}", user.getUsername());
        user.setCreator(userService.getUser(1L));

        user = userService.saveUser(user);

        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(user.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/user/{userId}/features", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Feature>> getUserFeatures(@PathVariable("userId") Long userId, UriComponentsBuilder uriComponentsBuilder) {

        LOG.debug("Request has been received for userId : {}", userId);
        return new ResponseEntity<List<Feature>>(userService.getUserFeatures(userId), HttpStatus.OK);

    }

    @RequestMapping(value = "/user/{userId}/routes", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Route>> getUserRoutes(@PathVariable("userId") Long userId, UriComponentsBuilder uriComponentsBuilder) {

        LOG.debug("Request has been received for userId : {}", userId);

        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<List<Route>>(userService.getUserRoutes(userId), HttpStatus.OK);

    }

    @RequestMapping(value = "/user/role/{roleId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<Role>> getUserByRoles(@PathVariable("roleId") Long roleId, UriComponentsBuilder uriComponentsBuilder) {

        LOG.debug("Request has been received for userId : {}", roleId);

        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(roleId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<List<Role>>(adminService.getAllRoles(), HttpStatus.OK);

    }

}
