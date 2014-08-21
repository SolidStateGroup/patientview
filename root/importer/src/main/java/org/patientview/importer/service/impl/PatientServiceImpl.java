package org.patientview.importer.service.impl;

import generated.Patientview;
import org.patientview.importer.service.PatientService;
import org.springframework.stereotype.Service;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Service
public class PatientServiceImpl extends AbstractServiceImpl<PatientServiceImpl> implements PatientService {

    @Override
    public void add(final Patientview patient) {
        LOG.info("Processing Patient" + patient.getPatient().getPersonaldetails().getNhsno());
    }
}
