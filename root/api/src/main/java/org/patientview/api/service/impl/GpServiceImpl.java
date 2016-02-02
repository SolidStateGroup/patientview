package org.patientview.api.service.impl;

import org.patientview.api.service.GpService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Service
public class GpServiceImpl extends AbstractServiceImpl<GpServiceImpl> implements GpService {

    @Inject
    private Properties properties;

    public void updateMasterTable() {
        String apiKey = properties.getProperty("nhschoices.api.key");
        System.out.println(apiKey);
    }
}
