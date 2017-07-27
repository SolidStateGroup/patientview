package org.patientview.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.DateTime;
import org.hl7.fhir.instance.model.Observation;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirDatabaseObservation;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.FhirObservation;
import org.patientview.persistence.model.ObservationHeading;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by james@solidstategroup.com
 * Created on 01/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ObservationService {

    void add(Patientview data, FhirLink fhirLink) throws FhirResourceException, SQLException;

    void add(FhirObservation fhirObservation, FhirLink fhirLink) throws FhirResourceException;

    Observation buildNonTestObservation(FhirObservation fhirObservation) throws FhirResourceException;

    Observation buildObservation(DateTime applies, String value, String comparator, String comments,
                                 ObservationHeading observationHeading, boolean editable) throws FhirResourceException;

    /**
     * Helper method to copy updated values from fhir observation to observation
     * Only updates applies, and value
     *
     * @param observation
     * @param applies
     * @param value
     * @return a new copied over observation with changes applied
     * @throws FhirResourceException
     */
    Observation copyObservation(Observation observation, Date applies, String value) throws FhirResourceException;

    /**
     * Delete all Observations from FHIR given a Set of FhirLink, used when deleting a patient and in migration.
     * @param fhirLinks Set of FhirLink
     * @throws FhirResourceException
     */
    void deleteAllExistingObservationData(Set<FhirLink> fhirLinks) throws FhirResourceException;

    /**
     * Natively delete Observations from FHIR by logical ID.
     * @param observationsUuidsToDelete List of UUID Observation logical IDs to delete
     * @throws FhirResourceException
     */
    void deleteObservations(List<UUID> observationsUuidsToDelete) throws FhirResourceException;

    void insertFhirDatabaseObservations(List<FhirDatabaseObservation> fhirDatabaseObservations)
            throws FhirResourceException;
}
