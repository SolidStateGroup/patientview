package org.patientview.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.persistence.EntityExistsException;

/**
 * Base controller containing exception handling
 *
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public class BaseController {


    private final static Logger LOG = LoggerFactory.getLogger(BaseController.class);

    @ExceptionHandler(EntityExistsException.class)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public String handleEntityException(EntityExistsException e) {
        LOG.error("Handling Entity Exception {}", e);
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


}
