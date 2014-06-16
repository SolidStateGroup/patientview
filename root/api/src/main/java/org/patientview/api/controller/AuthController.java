package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.service.AuthenticationService;
import org.patientview.persistence.model.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@RestController
public class AuthController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    @Inject
    private AuthenticationService authenticationService;

    @RequestMapping(value = "/auth/login", method = RequestMethod.POST)
    public ResponseEntity<UserToken> authenticate(UriComponentsBuilder uriComponentsBuilder,
                                                 HttpServletRequest request) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (StringUtils.isEmpty(username+password)) {
            LOG.debug("The username and password where not supplied");
            return new ResponseEntity<UserToken>(HttpStatus.BAD_REQUEST);

        }

        return new ResponseEntity<UserToken>(authenticationService.authenticate(username, password), HttpStatus.OK);

    }

}
