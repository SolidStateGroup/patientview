package org.patientview.api.service;

import org.hl7.fhir.instance.model.Encounter;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.EncounterTypes;

import java.util.List;
import java.util.UUID;

/**
 * Encounter service, used to retrieve patient encounters from FHIR, used for treatment and transplant status.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
public interface EncounterService {

    /**
     * Add a FHIR Encounter, used by UKT when adding kidney transplant status, also by migration.
     * @param fhirEncounter FhirEncounter, containing all fields required to store Encounter in FHIR
     * @param fhirLink FhirLink, link between patient and records in FHIR
     * @param organizationUuid UUID of FHIR Organization, same as Group
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    void addEncounter(FhirEncounter fhirEncounter, FhirLink fhirLink, UUID organizationUuid)
            throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get a List of FHIR Encounters given a patient UUID, typically retrieved from FhirLink resourceId.
     * @param patientUuid UUID of patient that is the subject of the Encounter
     * @return List of FHIR Encounters
     * @throws FhirResourceException
     */
    List<Encounter> get(UUID patientUuid) throws FhirResourceException;

    /**
     * Get a list of UUIDs representing FHIR encounters, used when deleting UKT data.
     * @param user User to find FHIR Encounter UUIDs for
     * @param encounterType EncounterTypes type of Encounter to find, e.g. EncounterTypes.TRANSPLANT_STATUS_KIDNEY
     * @return List of Encounter UUIDs
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    List<UUID> getUuidsByUserAndType(final User user, final EncounterTypes encounterType)
            throws ResourceNotFoundException, FhirResourceException;
}
