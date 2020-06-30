package org.patientview.api.service.impl;

import org.hl7.fhir.instance.model.ResourceType;
import org.patientview.api.service.ApiFileDataService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FileData;
import org.patientview.persistence.model.User;
import org.patientview.persistence.repository.FileDataRepository;
import org.patientview.persistence.repository.UserRepository;
import org.patientview.service.FileDataService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Service
@Transactional
public class ApiFileDataServiceImpl extends AbstractServiceImpl<ApiFileDataServiceImpl> implements ApiFileDataService {

    @Inject
    private FileDataRepository fileDataRepository;

    @Inject
    private FileDataService fileDataService;

    @Inject
    private UserRepository userRepository;


    @Override
    public FileData getFileData(Long userId, Long fileDataId) throws ResourceNotFoundException, FhirResourceException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Could not find user"));

        if (fileDataService.userHasFileData(user, fileDataId, ResourceType.DocumentReference)) {
            return fileDataRepository.getOne(fileDataId);
        } else {
            throw new ResourceNotFoundException("File not found");
        }
    }
}
