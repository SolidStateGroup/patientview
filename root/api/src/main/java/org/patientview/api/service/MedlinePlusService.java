package org.patientview.api.service;

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
}
