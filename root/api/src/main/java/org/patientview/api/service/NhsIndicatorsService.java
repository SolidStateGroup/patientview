package org.patientview.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.NhsIndicators;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Nhs Indicators service, used when retrieving and creating Nhs Indicator statistics.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 13/09/2016
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface NhsIndicatorsService {
    /**
     * Get all NhsIndicators (UNIT only), with option to store results in database.
     * @param store boolean store data in database
     * @return List of NhsIndicators
     * @throws ResourceNotFoundException thrown when group not found
     * @throws FhirResourceException thrown when error retrieving from FHIR
     * @throws JsonProcessingException thrown when converting data to save as JSON
     */
    List<NhsIndicators> getAllNhsIndicatorsAndStore(boolean store)
            throws ResourceNotFoundException, FhirResourceException, JsonProcessingException;

    /**
     * Given a Group ID, get NHS indicators statistics.
     * @param groupId Long ID of the Group to retrieve NHS indicators for
     * @return NhsIndicators object containing statistics
     * @throws ResourceNotFoundException thrown when Group does not exist
     * @throws FhirResourceException thrown when error retrieving from FHIR
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN })
    NhsIndicators getNhsIndicators(Long groupId) throws ResourceNotFoundException, FhirResourceException;

    /**
     * Get single NhsIndicators given group ID and date (as Long).
     * @param groupId Long ID of Group
     * @param date Long representation of Date
     * @return NhsIndicators
     * @throws ResourceNotFoundException Group or NhsIndicators not found
     * @throws IOException thrown when converting to transport object
     */
    @GroupMemberOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN })
    NhsIndicators getNhsIndicatorsByGroupAndDate(Long groupId, Long date) throws ResourceNotFoundException, IOException;

    /**
     * Get all NHS Indicator dates (when task was run).
     * @return List of Date
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.STAFF_ADMIN })
    List<Date> getNhsIndicatorsDates();
}
