package org.patientview.api.controller;

import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.EntityExistsException;
import java.lang.reflect.ParameterizedType;

/**
 * Base controller containing exception handling
 *
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public abstract class BaseController<T extends BaseController> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseController.class);

    public Class<T> getControllerClass()  {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) superclass.getActualTypeArguments()[0];
    }

    @ExceptionHandler(EntityExistsException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public String handleEntityException(EntityExistsException e) {
        LOG.info("Handling Entity Exception: " + e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String handleEntityException(ResourceNotFoundException e) {
        LOG.info("Could not find resource: " + e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public String handleSecurityException(SecurityException e) {
        LOG.info("Authentication failed for this resource");
        return e.getMessage();
    }

    @ExceptionHandler(ResourceForbiddenException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public String handleResourceForbiddenException(ResourceForbiddenException e) {
        LOG.info("Resource forbidden");
        return e.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception e) {
        LOG.error("Unhandled exception type {}", e.getCause());
        LOG.error("Unhandled exception", e);
        return e.getMessage();
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public String handleAuthenticationException(Exception e) {
        LOG.error("Login failed");
        return e.getMessage();
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public String handleUsernameException(Exception e) {
        LOG.error("Login failed");
        return e.getMessage();
    }
}
