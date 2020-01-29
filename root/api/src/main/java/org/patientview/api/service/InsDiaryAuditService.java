package org.patientview.api.service;

import org.patientview.persistence.model.InsDiaryAuditLog;

import java.util.List;

/**
 * Created by Pavlo Maksymchuk.
 */
public interface InsDiaryAuditService {

    /**
     * Creates new InsDiaryAuditLog record for patients if does not already exist.
     *
     * @param patientId
     */
    InsDiaryAuditLog add(Long patientId);

    /**
     * Deletes all InsDiaryAuditLog records for given patient
     *
     * @param patientId an id of the patient user tp delete records for
     */
    void deleteByPatient(Long patientId);

    /**
     * Find all InsDiaryRecord records
     *
     * @return a list of InsDiaryRecord objects
     */
    List<InsDiaryAuditLog> findAll();
}
