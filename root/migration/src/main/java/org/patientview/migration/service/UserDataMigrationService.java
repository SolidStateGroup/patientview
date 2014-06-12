package org.patientview.migration.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 06/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface UserDataMigrationService {

    void migrate();

}
