package org.patientview.api.controller;

import org.patientview.api.service.AdminService;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@RestController
public class UserController {


    private final static Logger LOG = LoggerFactory.getLogger(GroupController.class);


    @Inject
    private AdminService adminService;

    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public User createUser(@PathVariable("userId") Long userId) {
        return adminService.getUser(userId);

    }


    @RequestMapping(value = "/user", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> getUser(@RequestBody User user, UriComponentsBuilder uriComponentsBuilder) {

        LOG.debug("Request has been received for {}", user.getUsername());
        user.setCreator(adminService.getUser(1L));

        user = adminService.createUser(user);



        UriComponents uriComponents = uriComponentsBuilder.path("/user/{id}").buildAndExpand(user.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());
        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);

    }

}
