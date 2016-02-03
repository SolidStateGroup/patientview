package org.patientview.importer.service.impl;

import generated.Patientview;
import org.patientview.importer.service.GpLetterService;
import org.patientview.persistence.model.GpLetter;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Service
public class GpLetterServiceImpl extends AbstractServiceImpl<GpLetterServiceImpl> implements GpLetterService {

    @Override
    public void add(Patientview patientview) {

    }

    @Override
    public boolean hasValidGpDetails(Patientview patientview) {
        return false;
    }

    @Override
    public List<GpLetter> matchByGpDetails(Patientview patientview) {
        return null;
    }
}
