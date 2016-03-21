package org.patientview.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.Condition;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirCondition;
import org.patientview.persistence.model.FhirDatabaseEntity;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.enums.DiagnosisTypes;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 05/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ConditionService {

    void add(FhirCondition fhirCondition, FhirLink fhirLink) throws FhirResourceException;

    void add(Patientview data, FhirLink fhirLink) throws FhirResourceException;

    /**
     * Delete Condition by UUID subject ID and DiagnosisTypes type, used by API importer.
     * @param subjectId UUID subject ID
     * @param diagnosisTypes DiagnosisTypes type, e.g. DiagnosisTypes.DIAGNOSIS_EDTA
     * @throws FhirResourceException
     */
    void deleteBySubjectIdAndType(UUID subjectId, DiagnosisTypes diagnosisTypes) throws FhirResourceException;

    /**
     * Get a list of FHIR Conditions given a UUID from FhirLink representing the patient in FHIR.
     * @param patientUuid UUID representing the patient in FHIR
     * @return List of FHIR Conditions
     * @throws FhirResourceException
     */
    List<Condition> get(UUID patientUuid) throws FhirResourceException;

    FhirDatabaseEntity update(FhirCondition fhirCondition, FhirLink fhirLink, UUID existingConditionUuid)
            throws FhirResourceException;
}
