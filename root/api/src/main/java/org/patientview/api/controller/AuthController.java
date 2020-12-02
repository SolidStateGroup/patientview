package org.patientview.api.controller;

import com.wordnik.swagger.annotations.ApiOperation;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.Credentials;
import org.patientview.api.model.ForgottenCredentials;
import org.patientview.api.model.User;
import org.patientview.api.model.UserToken;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.UserService;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
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
 * RESTful interface for authentication actions, including login/logout, User password reset and viewing Users.
 *
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@RestController
public class AuthController extends BaseController<AuthController> {

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private UserService userService;

    /**
     * Method called by a user who has forgotten their own password. User's put in their username and email address and
     * if they match receive an email with login details. User's must change their password on next login. This can be
     * considered step 1 of the forgotten password process, where step 2 is used if they do not know their username or
     * email.
     * @param credentials ForgottenCredentials object with just username and email
     * @throws ResourceNotFoundException
     * @throws MailException
     * @throws MessagingException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/auth/forgottenpassword", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public void forgottenPassword(@RequestBody ForgottenCredentials credentials)
            throws ResourceNotFoundException, MailException, MessagingException, ResourceForbiddenException  {
        userService.resetPasswordByUsernameAndEmail(credentials.getUsername(), credentials.getEmail(),
                credentials.getCaptcha());
    }

    /**
     * Log in User, authenticate using username and password. Returns a token, which must be added to X-Auth-Token in
     * the header of all future requests.
     * @param credentials Credentials object containing username, password and api key (currently CKD only)
     * @return UserToken with token used to authenticate all future requests, passed as a X-Auth-Token header by the UI
     * @throws UsernameNotFoundException
     * @throws AuthenticationServiceException
     */
    @ApiOperation(value = "Log In", notes = "Authenticate using username and password, returns "
            + "token, which must be added to X-Auth-Token in header of all future requests")
    @RequestMapping(value = "/auth/login", method = RequestMethod.POST, consumes =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserToken> logIn(@RequestBody Credentials credentials)
            throws UsernameNotFoundException, AuthenticationServiceException {
        return new ResponseEntity<>(authenticationService.authenticate(credentials), HttpStatus.OK);
    }

    /**
     * Mobile specific login endpoint. Log in User, authenticate using username and password. Returns a token, which must be added to X-Auth-Token in
     * the header of all future requests.
     * @param credentials Credentials object containing username, password
     * @return UserToken with token used to authenticate all future requests, passed as a X-Auth-Token header by the UI
     * @throws UsernameNotFoundException
     * @throws AuthenticationServiceException
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/auth/loginmobile", method = RequestMethod.POST, consumes =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserToken> logInMobile(@RequestBody Credentials credentials)
            throws UsernameNotFoundException, AuthenticationServiceException {
        return new ResponseEntity<>(
                authenticationService.authenticateMobile(credentials), HttpStatus.OK);
    }

    /**
     * Log out User, clearing their session and deleting the token associated with their account, invalidating all
     * future requests with this token.
     * @param token String token associated with User
     * @throws AuthenticationServiceException
     */
    @ApiOperation(value = "Log Out", notes = "Log Out")
    @RequestMapping(value = "/auth/logout/{token}", method = RequestMethod.DELETE)
    @ResponseBody
    public void logOut(@PathVariable("token") String token) throws AuthenticationServiceException {
        authenticationService.logout(token, false);
    }

    /**
     * Get basic user information (User) given the token produced when a user successfully logs in. Used by CKD.
     * @param token String token associated with a successfully logged in user
     * @return User object containing relevant user information
     * @throws AuthenticationServiceException
     * @throws ResourceForbiddenException
     */
    @ApiOperation(value = "Get Basic User Information", notes = "Once logged in and have a token, get basic user "
            + "information including group role membership")
    @RequestMapping(value = "/auth/{token}/basicuserinformation", method = RequestMethod.GET)
    public ResponseEntity<User> getBasicUserInformation(@PathVariable("token") String token)
            throws AuthenticationServiceException, ResourceForbiddenException {
        return new ResponseEntity<>(authenticationService.getBasicUserInformation(token), HttpStatus.OK);
    }

    /**
     * Get user information (security roles, groups etc) given the token produced when a user successfully logs in.
     * Performed after login.
     * @return UserToken object containing relevant user information and static data
     * @throws AuthenticationServiceException
     * @throws ResourceForbiddenException
     */
    @ExcludeFromApiDoc
    @ApiOperation(value = "Get User Information", notes = "Once logged in and have a token, get all relevant user "
            + "information and static data used by front end")
    @RequestMapping(value = "/auth/userinformation", method = RequestMethod.POST)
    public ResponseEntity<UserToken> getUserInformation(@RequestBody UserToken userToken)
            throws AuthenticationServiceException, ResourceNotFoundException, ResourceForbiddenException {
        return new ResponseEntity<>(authenticationService.getUserInformation(userToken), HttpStatus.OK);
    }

    /**
     * Switch from the current User to another by authenticating and returning a token associated with the new user.
     * This is the equivalent of logging in as the new User and acting as them entirely.
     * @param userId The User ID to switch to
     * @return String token used to authenticate all future requests, passed as a X-Auth-Token header by the UI
     * @throws AuthenticationServiceException
     */
    @ExcludeFromApiDoc
    @ApiOperation(value = "Switch User", notes = "Switch to a patient, equivalent to logging in as that user")
    @RequestMapping(value = "/auth/switchuser/{userId}", method = RequestMethod.GET)
    public ResponseEntity<String> switchUser(@PathVariable("userId") Long userId)
            throws AuthenticationServiceException {
        return new ResponseEntity<>(authenticationService.switchToUser(userId), HttpStatus.OK);
    }

    /**
     * Switch back to the previous user using the token associated with the previous user.
     * @param token String token associated with the previous user
     * @param userId ID of the user to switch back
     * @return String token used to authenticate all future requests, passed as a X-Auth-Token header by the UI
     * @throws AuthenticationServiceException
     */
    @ExcludeFromApiDoc
    @ApiOperation(value = "Switch to Previous User", notes = "Switch back to original user after viewing a patient")
    @RequestMapping(value = "/auth/{token}/switchuser/{userId}", method = RequestMethod.GET)
    public ResponseEntity<String> switchToPreviousUser(@PathVariable("token") String token,
                                                       @PathVariable("userId") Long userId)
            throws AuthenticationServiceException {
        return new ResponseEntity<>(authenticationService.switchBackFromUser(userId, token), HttpStatus.OK);
    }

    /**
     * Utility method to get status of API, used by external website monitoring tools.
     * @return String "API OK" to show API is functioning as expected
     */
    @ExcludeFromApiDoc
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> testService() {
        return new ResponseEntity<>("API OK", HttpStatus.OK);
    }
}
