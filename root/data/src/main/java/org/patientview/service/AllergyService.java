package org.patientview.service;

import generated.Patientview;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirAllergy;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 17/11/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AllergyService {

    void add(Patientview data, FhirLink fhirLink) throws FhirResourceException, SQLException;

    void add(FhirAllergy fhirAllergy, FhirLink fhirLink) throws FhirResourceException;

    List<FhirAllergy> getBySubject(UUID subjectUuid) throws FhirResourceException;
}
