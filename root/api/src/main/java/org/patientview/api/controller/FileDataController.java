package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.ApiFileDataService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FileData;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * RESTful interface for file data, stored in FHIR and imported by multiple Groups.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/06/2016
 */
@ExcludeFromApiDoc
@RestController
public class FileDataController extends BaseController<FileDataController> {

    @Inject
    private ApiFileDataService apiFileDataService;

    /**
     * Download a file, given User ID and FileData ID.
     * @param userId ID of User to download file for
     * @param fileDataId ID of FileData containing binary data
     * @return HttpEntity to allow client to download in browser
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    @RequestMapping(value = "/user/{userId}/file/{fileDataId}/download", method = RequestMethod.GET)
    @ResponseBody
    public HttpEntity<byte[]> download(@PathVariable("userId") Long userId,
                                       @PathVariable("fileDataId") Long fileDataId)
            throws ResourceNotFoundException, FhirResourceException {
        FileData fileData = apiFileDataService.getFileData(userId, fileDataId);

        if (fileData != null) {
            HttpHeaders header = new HttpHeaders();
            String[] contentTypeArr = fileData.getType().toLowerCase().split("/");
            if (contentTypeArr.length == 2) {
                header.setContentType(new MediaType(contentTypeArr[0], contentTypeArr[1]));
            }
            header.set("Content-Disposition", "inline; filename=" + fileData.getName().replace(" ", "_"));
            header.setContentLength(fileData.getContent().length);
            return new HttpEntity<>(fileData.getContent(), header);
        }

        return null;
    }
}
