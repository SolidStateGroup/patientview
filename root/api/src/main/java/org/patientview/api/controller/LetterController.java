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
 * RESTful interface for patient letters, stored in FHIR and imported by multiple Groups.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@RestController
public class LetterController extends BaseController<LetterController> {

    @Inject
    private LetterService letterService;

    /**
     * Delete a letter based on User ID, Group ID (Group that imported the letter) and date (as Long).
     * @param userId ID of User to delete letter for
     * @param groupId ID of Group that originally imported the letter
     * @param date Long representation of letter date
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/group/{groupId}/letters/{date}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteLetter(@PathVariable("userId") Long userId, @PathVariable("groupId") Long groupId,
                             @PathVariable("date") Long date)
            throws FhirResourceException, ResourceNotFoundException {
        letterService.delete(userId, groupId, date);
    }

    /**
     * Get a List of all a User's letters, retrieved from FHIR.
     * @param userId ID of User to retrieve letters for
     * @return List of letters in FhirDocumentReference format
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/letters", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirDocumentReference>> getAllLetters(@PathVariable("userId") Long userId)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(letterService.getByUserId(userId), HttpStatus.OK);
    }
}
