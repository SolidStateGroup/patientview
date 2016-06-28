package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.service.DocumentService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
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
 * RESTful interface for documentreference retrieval, stored in FHIR and imported by multiple Groups.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/06/2016
 */
@ExcludeFromApiDoc
@RestController
public class DocumentController extends BaseController<DocumentController> {

    @Inject
    private DocumentService documentService;

    /**
     * Get a List of all a User's documentreference by class e.g. YOUR_HEALTH_SURVEY, retrieved from FHIR.
     * @param userId ID of User to retrieve documents for
     * @param fhirClass String of documentreference class to find
     * @return List of documents in FhirDocumentReference format
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/documents/{fhirClass}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirDocumentReference>> getByUserIdAndClass(
            @PathVariable("userId") Long userId, @PathVariable("fhirClass") String fhirClass)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(documentService.getByUserIdAndClass(userId, fhirClass, null, null), HttpStatus.OK);
    }
}
