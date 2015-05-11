package org.patientview.importer.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ConditionService {

    public void add(Patientview data, FhirLink fhirLink) throws FhirResourceException, SQLException;
}
