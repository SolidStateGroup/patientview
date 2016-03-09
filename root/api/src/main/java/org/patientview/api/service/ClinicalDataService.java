package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.persistence.model.FhirClinicalData;
import org.patientview.persistence.model.ServerResponse;
import org.patientview.persistence.model.enums.RoleName;

/**
 * Clinical data service, used by API importer to store treatment and diagnoses in FHIR.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 09/03/2016
 */
public interface ClinicalDataService {

    @RoleOnly(roles = { RoleName.IMPORTER })
    ServerResponse importClinicalData(FhirClinicalData fhirClinicalData);
}
