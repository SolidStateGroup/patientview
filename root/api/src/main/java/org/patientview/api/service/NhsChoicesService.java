package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;

/**
 * NHS Choices service, for retrieving data from NHS Choices
 *
 * Created by jamesr@solidstategroup.com
 * Created on 18/01/2016
 */
public interface NhsChoicesService {

    @RoleOnly
    void updateOrganisations();
}
