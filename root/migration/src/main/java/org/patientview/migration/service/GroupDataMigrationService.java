package org.patientview.migration.service;

import org.patientview.migration.util.exception.JsonMigrationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface GroupDataMigrationService {

    void createGroups() throws JsonMigrationException;
}
