package org.patientview.migration.service;

import org.patientview.persistence.model.MigrationUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 29/10/2014
 */
@Transactional
public interface AsyncService {

    public void callApiMigrateUser(MigrationUser user) throws Exception;
}
