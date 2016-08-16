package org.patientview.api.service;

import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeExternalStandard;

/**
 * MedlinePlus service, for retrieving data from MedlinePlus webservice
 */
public interface MedlinePlusService {
    /**
     * @param entityCode
     */
    void setLink(Code entityCode);

    /**
     * @param entityCode
     * @param codeExternalEntity
     */
    void setCodeExternalStandardLink(Code entityCode, CodeExternalStandard codeExternalEntity);

    /**
     * Reads codes from excel file in resources and either adds or updates ICD10
     * code to Code which maps to NHS codes.
     * <p>
     * Also will add or update any MedlinePlus links
     */
    void syncICD10Codes() throws ResourceNotFoundException;
}
