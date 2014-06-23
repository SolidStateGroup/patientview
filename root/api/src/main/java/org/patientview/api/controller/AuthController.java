package org.patientview.api.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.http.auth.AuthenticationException;
import org.patientview.api.controller.model.Credentials;
import org.patientview.api.service.AuthenticationService;
import org.patientview.persistence.model.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by james@solidstategroup.com
 * Created on 13/06/2014
 */
@RestController
public class AuthController extends BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    @Inject
    private AuthenticationService authenticationService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> testService() {
        return new ResponseEntity<String>("API OK", HttpStatus.OK);
    }

    @RequestMapping(value = "/auth/login", method = RequestMethod.POST, consumes =  MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserToken> authenticate(@RequestBody Credentials credentials,
                                                  UriComponentsBuilder uriComponentsBuilder,
                                                  HttpServletRequest request)
            throws UsernameNotFoundException, AuthenticationException{

        if (StringUtils.isEmpty(credentials.getUsername())) {
            LOG.debug("A username must be supplied");
            return new ResponseEntity<UserToken>(HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isEmpty(credentials.getPassword())) {
            LOG.debug("A password must be supplied");
            return new ResponseEntity<UserToken>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<UserToken>(authenticationService.authenticate(credentials.getUsername(),
                credentials.getPassword()), HttpStatus.OK);

    }

    @RequestMapping(value = "/auth/logout/{token}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> deleteToken(@PathVariable("token") String token) throws AuthenticationException{
        authenticationService.logout(token);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @ExceptionHandler(AuthenticationException.class)
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
