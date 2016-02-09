package org.patientview.api.controller;

import net.lingala.zip4j.exception.ZipException;
import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.GpDetails;
import org.patientview.api.service.GpService;
import org.patientview.config.exception.VerificationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

/**
 * RESTful interface for handling GP account creation and managing required data, including
 * GP data from external sources
 *
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@ExcludeFromApiDoc
@RestController
public class GpController extends BaseController<GpController> {

    @Inject
    private GpService gpService;

    @RequestMapping(value = "/gp/claim", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<GpDetails> claim(@RequestBody GpDetails gpDetails) throws VerificationException {
        return new ResponseEntity<>(gpService.claim(gpDetails), HttpStatus.OK);
    }

    /**
     * Update the GP master table from external sources (XLS files)
     */
    @RequestMapping(value = "/gp/updatemastertable", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateMasterTable() throws IOException, ZipException {
        return new ResponseEntity<>(gpService.updateMasterTable(), HttpStatus.OK);
    }

    @RequestMapping(value = "/gp/validatedetails", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<GpDetails> validateDetails(@RequestBody GpDetails gpDetails) throws VerificationException {
        return new ResponseEntity<>(gpService.validateDetails(gpDetails), HttpStatus.OK);
    }
}
