package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.Credentials;
import org.patientview.api.model.ForgottenCredentials;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.UserService;
import org.patientview.api.model.UserToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@RestController
public class AuthController extends BaseController<AuthController> {

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private UserService userService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> testService() {
        return new ResponseEntity<>("API OK", HttpStatus.OK);
    }

    // switch to previous user using previous auth token, todo: requires security
    @RequestMapping(value = "/auth/{token}/switchuser/{userId}", method = RequestMethod.GET)
    public ResponseEntity<UserToken> switchToPreviousUser(@PathVariable("token") String token,
            @PathVariable("userId") Long userId) throws AuthenticationServiceException {
        return new ResponseEntity<>(authenticationService.switchUser(userId, token), HttpStatus.OK);
    }

    // switch to another user by authenticating and returning token, todo: requires security
    @RequestMapping(value = "/auth/switchuser/{userId}", method = RequestMethod.GET)
    public ResponseEntity<UserToken> switchUser(@PathVariable("userId") Long userId)
            throws AuthenticationServiceException {
        return new ResponseEntity<>(authenticationService.switchUser(userId, null), HttpStatus.OK);
    }

    @RequestMapping(value = "/auth/login", method = RequestMethod.POST, consumes =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserToken> authenticate(@RequestBody Credentials credentials)
            throws UsernameNotFoundException, AuthenticationServiceException {

        if (StringUtils.isEmpty(credentials.getUsername())) {
            LOG.debug("A username must be supplied");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(credentials.getPassword())) {
            LOG.debug("A password must be supplied");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(authenticationService.authenticate(credentials.getUsername(),
                credentials.getPassword()), HttpStatus.OK);

    }

    @RequestMapping(value = "/auth/forgottenpassword", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> forgottenPassword(@RequestBody ForgottenCredentials credentials)
            throws ResourceNotFoundException {
        userService.resetPasswordByUsernameAndEmail(credentials.getUsername(), credentials.getEmail());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @RequestMapping(value = "/auth/logout/{token}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteToken(@PathVariable("token") String token)
            throws AuthenticationServiceException {
        authenticationService.logout(token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public String handleAuthenticationException(Exception e) {
        LOG.error("Login failed");
        return e.getMessage();
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public String handleUsernameException(Exception e) {
        LOG.error("Login failed");
        return e.getMessage();
    }

}
