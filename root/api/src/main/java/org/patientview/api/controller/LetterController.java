package org.patientview.api.controller;

import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.service.LetterService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@RestController
public class LetterController extends BaseController<LetterController> {

    @Inject
    private LetterService letterService;

    @RequestMapping(value = "/user/{userId}/letters", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirDocumentReference>> getAllLetters(@PathVariable("userId") Long userId)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(letterService.getByUserId(userId), HttpStatus.OK);
    }

    @RequestMapping(value = "/user/{userId}/letters/{date}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteLetters(@PathVariable("userId") Long userId, @PathVariable("date") Long date)
            throws FhirResourceException, ResourceNotFoundException {
        letterService.delete(userId, date);
    }
}
