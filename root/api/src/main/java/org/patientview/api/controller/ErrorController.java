package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Not currently used, originally to manage redirects from authentication failures.
 *
 * @deprecated
 * Created by james@solidstategroup.com
 * Created on 23/07/2014
 */
@RestController
@ExcludeFromApiDoc
public class ErrorController extends BaseController<ErrorController> {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorController.class);

    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public String handleError() {
        LOG.error("Handling error path");
        return "Not Authorised";
    }

}
