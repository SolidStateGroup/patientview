package org.patientview.migration.service;

import org.patientview.migration.util.exception.JsonMigrationException;
import org.patientview.persistence.model.Feature;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.Role;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AdminDataMigrationService {

    void init() throws JsonMigrationException;

    Lookup getLookupByName(String value);

    Feature getFeatureByName(String value);

    Group getGroupByCode(String code);

    Role getRoleByName(RoleName name);

    Group getGroupByName(String name);

    void migrate();
}
