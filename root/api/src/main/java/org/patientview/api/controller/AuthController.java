package org.patientview.api.controller;

import org.patientview.api.service.AuthenticationService;
import org.patientview.persistence.model.UserToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@RestController
public class AuthController {

    @Inject
    private AuthenticationService authenticationService;

    @RequestMapping(value = "/auth/login", method = RequestMethod.POST)
    public ResponseEntity<UserToken> createGroup(@RequestParam("username") String username,
                                             @RequestParam("password") String password,
                                             UriComponentsBuilder uriComponentsBuilder,
                                             HttpServletResponse response) {
        return new ResponseEntity<UserToken>(authenticationService.authenticate(username, password), HttpStatus.OK);

    }

}
