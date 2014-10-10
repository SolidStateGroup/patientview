package org.patientview.importer.service;

import generated.Patientview;
import org.patientview.persistence.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 07/10/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface DocumentReferenceService {

    public void add(Patientview data, FhirLink fhirLink) throws FhirResourceException, SQLException;

    public void deleteBySubjectId(UUID subjectId) throws FhirResourceException, SQLException;
}
