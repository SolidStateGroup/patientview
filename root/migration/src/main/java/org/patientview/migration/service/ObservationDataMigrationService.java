package org.patientview.migration.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 01/12/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ObservationDataMigrationService {
    void bulkObservationCreate(String unitCode1, String unitCode2, Long usersToInsertObservations, Long observationCount);
}
