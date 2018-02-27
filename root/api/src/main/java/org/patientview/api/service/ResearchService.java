package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.ResearchStudy;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Research service, for the management and retrieval of Research Studies. Research are made visible to specific
 * criteria of patients, based on age/condition etc
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface ResearchService {

    /**
     * Add a NewsItem.
     *
     * @param researchStudy Research Study item to add
     * @return Long ID of the newly added NewsItem
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN})
    Long add(ResearchStudy researchStudy);

    /**
     * Delete a Research Study.
     *
     * @param researchItemId ID of ResearchStudy to delete
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN})
    void delete(Long researchItemId) throws ResourceNotFoundException, ResourceForbiddenException;


    /**
     * Get a single NewsItem.
     *
     * @param researchItemId ID of NewsItem to retrieve
     * @return NewsItem object
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN})
    ResearchStudy get(Long researchItemId) throws ResourceNotFoundException, ResourceForbiddenException;


    /**
     * Get all research studies
     *
     * @return ResearchStudy list
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN, RoleName.GLOBAL_ADMIN})
    Page<ResearchStudy> getAll() throws ResourceNotFoundException, ResourceForbiddenException;


    /**
     * Get all research studies
     *
     * @return ResearchStudy list
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.PATIENT, RoleName.UNIT_ADMIN, RoleName.GLOBAL_ADMIN})
    Page<ResearchStudy> getAllForUser(Long userId, boolean limitResults, Pageable pageable) throws ResourceNotFoundException, ResourceForbiddenException, FhirResourceException;

    /**
     * Update a NewsItem.
     *
     * @param researchItem Research Study to update
     * @throws ResourceNotFoundException
     * @throws ResourceForbiddenException
     */
    @RoleOnly(roles = {RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN})
    void save(ResearchStudy researchItem) throws ResourceNotFoundException, ResourceForbiddenException;
}
