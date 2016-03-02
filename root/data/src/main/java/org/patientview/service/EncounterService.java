package org.patientview.service;

import generated.Patientview;
import org.hl7.fhir.instance.model.Encounter;
import org.hl7.fhir.instance.model.ResourceReference;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirLink;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/09/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface EncounterService {

    void add(Patientview data, FhirLink fhirLink, ResourceReference groupReference)
            throws FhirResourceException, SQLException;

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
     * Delete FHIR encounters for a User given type, used when deleting UKT data.
     * @param user User to delete FHIR Encounters for
     * @param encounterType EncounterTypes type of Encounter to delete, e.g. EncounterTypes.TRANSPLANT_STATUS_KIDNEY
     * @throws ResourceNotFoundException
     * @throws FhirResourceException
     */
    void deleteByUserAndType(final User user, final EncounterTypes encounterType)
            throws ResourceNotFoundException, FhirResourceException;
}
