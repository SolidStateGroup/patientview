package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeExternalStandard;

/**
 * MedlinePlus service, for retrieving data from MedlinePlus webservice
 */
public interface MedlinePlusService {
    /**
     * Sets a Link for all the CodeExternalStandard for the Code
     *
     * @param entityCode
     */
    void setLink(Code entityCode);

    /**
     * Sets a Link for CodeExternalStandard
     *
     * @param entityCode
     * @param codeExternalEntity
     */
    void setCodeExternalStandardLink(Code entityCode, CodeExternalStandard codeExternalEntity);

    /**
     * Reads codes from excel file in resources and either adds or updates ICD10
     * code to Code which maps to NHS codes.
     * <p>
     * Also will add or update any MedlinePlus links
     *
     * @throws ResourceNotFoundException
     * @throws ImportResourceException
     */
    @RoleOnly
    void syncICD10Codes() throws ResourceNotFoundException, ImportResourceException;
}
