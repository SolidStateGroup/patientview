package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.patientview.api.model.Credentials;
import org.patientview.api.model.ForgottenCredentials;
import org.patientview.api.model.UserToken;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.mail.MessagingException;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@RestController
public class AuthController extends BaseController<AuthController> {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private UserService userService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> testService() {
        return new ResponseEntity<>("API OK", HttpStatus.OK);
    }

    // switch to previous user using previous auth token
    @CacheEvict(value = "unreadConversationCount", allEntries = true)
    @RequestMapping(value = "/auth/{token}/switchuser/{userId}", method = RequestMethod.GET)
    public ResponseEntity<String> switchToPreviousUser(@PathVariable("token") String token,
            @PathVariable("userId") Long userId) throws AuthenticationServiceException {
        return new ResponseEntity<>(authenticationService.switchBackFromUser(userId, token), HttpStatus.OK);
    }

    // switch to another user by authenticating and returning token
    @CacheEvict(value = "unreadConversationCount", allEntries = true)
    @RequestMapping(value = "/auth/switchuser/{userId}", method = RequestMethod.GET)
    public ResponseEntity<String> switchUser(@PathVariable("userId") Long userId)
            throws AuthenticationServiceException {
        return new ResponseEntity<>(authenticationService.switchToUser(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/auth/login", method = RequestMethod.POST, consumes =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> authenticate(@RequestBody Credentials credentials)
            throws UsernameNotFoundException, AuthenticationServiceException {

        if (StringUtils.isEmpty(credentials.getUsername())) {
            LOG.debug("A username must be supplied");
            throw new AuthenticationServiceException("Incorrect username or password");
        }

        if (StringUtils.isEmpty(credentials.getPassword())) {
            LOG.debug("A password must be supplied");
            throw new AuthenticationServiceException("Incorrect username or password");
        }

        return new ResponseEntity<>(authenticationService.authenticate(
                credentials.getUsername(), credentials.getPassword()), HttpStatus.OK);
    }

    // populate userToken with user information (security roles, groups etc) performed after login
    // todo: requires security
    @RequestMapping(value = "/auth/{token}/userinformation", method = RequestMethod.GET)
    public ResponseEntity<UserToken> getUserInformation(@PathVariable("token") String token)
            throws AuthenticationServiceException, ResourceForbiddenException {
        return new ResponseEntity<>(authenticationService.getUserInformation(token), HttpStatus.OK);
    }

    // Stage 1 of Forgotten Password, user knows username and email
    @RequestMapping(value = "/auth/forgottenpassword", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void forgottenPassword(@RequestBody ForgottenCredentials credentials)
            throws ResourceNotFoundException, MailException, MessagingException  {
        userService.resetPasswordByUsernameAndEmail(credentials.getUsername(), credentials.getEmail());
    }

    @RequestMapping(value = "/auth/logout/{token}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteToken(@PathVariable("token") String token) throws AuthenticationServiceException {
        authenticationService.logout(token);
    }
}
