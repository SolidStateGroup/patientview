package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;

/**
 * GP Service, for managing master table of GPs from external sources
 *
 * Created by jamesr@solidstategroup.com
 * Created on 02/01/2016
 */
public interface GpService {

    @RoleOnly
    void updateMasterTable();
}
