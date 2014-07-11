package org.patientview.migration.service;

import org.patientview.Feature;
import org.patientview.Group;
import org.patientview.Lookup;
import org.patientview.Role;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface AdminDataMigrationService {
    Group getRenal();

    Group getDiabetes();

    Group getIbd();

    Lookup getLookupByName(String value);

    Feature getFeatureByName(String value);

    Group getGroupByCode(String code);

    Role getRoleByName(String name);

    Group getGroupByName(String name);

    void migrate();
}
