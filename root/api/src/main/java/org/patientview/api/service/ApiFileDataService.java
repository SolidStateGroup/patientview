package org.patientview.api.service;

import org.patientview.api.annotation.UserOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FileData;

/**
 * FileData service for retrieving FileData for patients.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 21/06/2016
 */
public interface ApiFileDataService {

    @UserOnly
    FileData getFileData(Long userId, Long fileDataId) throws ResourceNotFoundException, FhirResourceException;
}
