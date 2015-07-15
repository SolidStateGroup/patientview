package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.FhirDiagnosticReport;
import org.patientview.api.service.DiagnosticService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FileData;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

/**
 * RESTful interface for Diagnostics, not currently used heavily but found in UI from patient results page.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 05/10/2014
 */
@RestController
@ExcludeFromApiDoc
public class DiagnosticController extends BaseController<DiagnosticController> {

    @Inject
    private DiagnosticService diagnosticService;

    /**
     * Download a letter, given User ID and FileData ID.
     * @param userId ID of User to download letter for
     * @param fileDataId ID of FileData containing binary letter data
     * @return HttpEntity to allow client to download in browser
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @RequestMapping(value = "/user/{userId}/diagnostics/{fileDataId}/download", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> download(@PathVariable("userId") Long userId, @PathVariable("fileDataId") Long fileDataId)
            throws ResourceNotFoundException, FhirResourceException {
        FileData fileData = diagnosticService.getFileData(userId, fileDataId);

        if (fileData != null) {
            HttpHeaders header = new HttpHeaders();
            String[] contentTypeArr = fileData.getType().split("/");
            if (contentTypeArr.length == 2) {
                header.setContentType(new MediaType(contentTypeArr[0], contentTypeArr[1]));
            }
            header.set("Content-Disposition", "attachment; filename=" + fileData.getName().replace(" ", "_"));
            header.setContentLength(fileData.getContent().length);
            return new HttpEntity<>(fileData.getContent(), header);
        }

        return null;
    }

    /**
     * Get Diagnostics for a patient from FHIR given a User ID.
     * @param userId ID of User to get Diagnostics for
     * @return List of diagnostic reports, retrieved from FHIR
     * @throws FhirResourceException
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/user/{userId}/diagnostics", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<List<FhirDiagnosticReport>> getAllDiagnostics(@PathVariable("userId") Long userId)
            throws FhirResourceException, ResourceNotFoundException {
        return new ResponseEntity<>(diagnosticService.getByUserId(userId), HttpStatus.OK);
    }
}
