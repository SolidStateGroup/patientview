package org.patientview.api.service.impl;

import org.patientview.api.service.FileDataService;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.repository.FileDataRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 14/11/2014
 */
@Service
public class FileDataServiceImpl extends AbstractServiceImpl<FileDataServiceImpl> implements FileDataService {

    @Inject
    private FileDataRepository fileDataRepository;

    @Override
    public byte[] base64ToByteArray(String base64) {
        return CommonUtils.base64ToByteArray(base64);
    }

    @Override
    public String byteArrayToBase64(byte[] byteArray) {
        return CommonUtils.byteArrayToBase64(byteArray);
    }

    @Override
    public void delete(Long id) {
        fileDataRepository.delete(id);
    }
}
