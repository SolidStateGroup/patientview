package org.patientview.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ObservationService {

    public void add(Patientview data, FhirLink fhirLink) throws FhirResourceException, SQLException;
}
