package org.patientview.api.controller;

import net.lingala.zip4j.exception.ZipException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.MigrationException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.VerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.mail.MessagingException;
import javax.persistence.EntityExistsException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.net.SocketException;

/**
 * RESTful base controller containing exception handling, other controllers extend from this
 *
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public abstract class BaseController<T extends BaseController> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseController.class);

    public Class<T> getControllerClass() {
        ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<T>) superclass.getActualTypeArguments()[0];
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public String handleAuthenticationException(Exception e) {
        //LOG.error("Login failed: " + e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(EntityExistsException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public String handleEntityException(EntityExistsException e) {
        LOG.info("Handling Entity Exception: " + e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception e, HttpServletRequest request) {
        LOG.error("Unhandled exception for uri '{}' query '{}'", request.getRequestURI(), request.getQueryString());
        LOG.error("Unhandled exception ", e);
        return "Server error, unhandled exception";
    }

    @ExceptionHandler(IOException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleIoException(IOException e) {
        LOG.error("IO exception: ", e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(MailException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleMailException(MailException e) {
        LOG.error("Mail exception {}", e);
        return e.getMessage();
    }

    @ExceptionHandler(MessagingException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleMessagingException(MessagingException e) {
        LOG.error("Messaging exception {}", e);
        return e.getMessage();
    }

    @ExceptionHandler(MigrationException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleMigrationException(MigrationException e) {
        LOG.error("Migration exception {}", e);
        return e.getMessage();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String handleNotFoundException(ResourceNotFoundException e) {
        LOG.info("Could not find resource: " + e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(NumberFormatException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleNumberFormatException(NumberFormatException e) {
        LOG.error("NumberFormatException exception {}", e);
        return "Number format exception";
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleNullPointerException(NullPointerException e) {
        LOG.error("Null Pointer {}", e);
        return e.getMessage();
    }

    @ExceptionHandler(ResourceForbiddenException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public String handleResourceForbiddenException(ResourceForbiddenException e) {
        LOG.info("Resource forbidden: " + e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(ResourceInvalidException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleResourceInvalidException(ResourceInvalidException e) {
        LOG.info("Resource invalid: " + e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public String handleSecurityException(SecurityException e) {
        LOG.info("Authentication failed for this resource", e);
        return e.getMessage();
    }

    @ExceptionHandler(SocketException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleSocketException(SocketException e) {
        LOG.error("Socket exception {}", e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public String handleUsernameException(Exception e) {
        //LOG.error("Login failed: " + e.getMessage());
        return e.getMessage();
    }

    @ExceptionHandler(VerificationException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleVerificationException(VerificationException e) {
        LOG.error("Verification exception {}", e);
        return e.getMessage();
    }

    @ExceptionHandler(ZipException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String handleZipException(ZipException e) {
        LOG.error("ZipException exception {}", e);
        return "Zip exception";
    }

    @ExceptionHandler(FhirResourceException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleFhirResourceException(FhirResourceException e) {
        LOG.error("FhirResourceException exception {}", e);
        return "FhirResource exception";
    }
}
