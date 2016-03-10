package org.patientview.service;

import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.User;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * For binary files, e.g. Letters.
 * Created by jamesr@solidstategroup.com
 * Created on 05/05/2015
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface FileDataService {
    byte[] base64ToByteArray(String base64);

    String byteArrayToBase64(byte[] byteArray);

    void delete(Long id);

    FileData get(Long id);

    boolean userHasFileData(User user, Long fileDataId, ResourceType resourceType) throws FhirResourceException;
}
