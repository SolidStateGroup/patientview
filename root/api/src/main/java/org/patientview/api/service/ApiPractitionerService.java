package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.enums.RoleName;

/**
 * Practitioner service, for handling updates to a patient's practitioner by API importer
 *
 * Created by james@solidstategroup.com
 * Created on 04/03/2016
 */
public interface ApiPractitionerService {

    /**
     * Update or create practitioner details (used by API importer)
     * @param fhirPractitioner details to update
     * @return ServerResponse with success/error messages
     */
    @RoleOnly(roles = { RoleName.IMPORTER })
    ServerResponse importPractitioner(FhirPractitioner fhirPractitioner);
}
