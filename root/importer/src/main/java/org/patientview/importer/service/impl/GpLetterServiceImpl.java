package org.patientview.importer.service.impl;

import generated.Patientview;
import org.apache.commons.lang3.StringUtils;
import org.patientview.importer.service.GpLetterService;
import org.patientview.persistence.model.GpLetter;
import org.patientview.persistence.repository.GpMasterRepository;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/02/2016
 */
@Service
public class GpLetterServiceImpl extends AbstractServiceImpl<GpLetterServiceImpl> implements GpLetterService {

    @Inject
    GpMasterRepository gpMasterRepository;

    @Override
    public void add(Patientview patientview) {

    }

    @Override
    public boolean hasValidPracticeDetails(Patientview patientview) {
        // check gpdetails section is present
        if (patientview.getGpdetails() == null) {
            return false;
        }

        Patientview.Gpdetails gp = patientview.getGpdetails();

        // check postcode is set
        if (StringUtils.isEmpty(gp.getGppostcode())) {
            return false;
        }

        // validate at least 2 of address1, address2, address3 is present
        int fieldCount = 0;
        if (StringUtils.isNotEmpty(gp.getGpaddress1())) {
            fieldCount++;
        }
        if (StringUtils.isNotEmpty(gp.getGpaddress2())) {
            fieldCount++;
        }
        if (StringUtils.isNotEmpty(gp.getGpaddress3())) {
            fieldCount++;
        }

        return fieldCount > 1;
    }

    @Override
    public boolean hasValidPracticeDetailsCheckMaster(Patientview patientview) {
        // check gpdetails section is present
        if (patientview.getGpdetails() == null) {
            return false;
        }

        Patientview.Gpdetails gp = patientview.getGpdetails();

        // check postcode is set
        if (StringUtils.isEmpty(gp.getGppostcode())) {
            return false;
        }

        // validate postcode exists in GP master table and only one record
        return gpMasterRepository.findByPostcode(gp.getGppostcode()).size() == 1;
    }

    @Override
    public List<GpLetter> matchByGpDetails(Patientview patientview) {
        return null;
    }
}
