package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.GetParameters;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * Code service, used for creating, cloning, deleting, retrieving and modifying Codes.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 25/06/2014
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface CodeService extends CrudService<Code> {

    /**
     * Create a new Code.
     * @param code Code object containing all required properties
     * @return Code object, newly created (note: consider only returning ID or HTTP OK)
     * @throws ResourceNotFoundException
     * @throws EntityExistsException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Code add(Code code) throws EntityExistsException;

    /**
     * Make a copy of an existing Code, typically to avoid having to re-enter large amounts of similar information in
     * UI.
     * @param codeId ID of Code to clone
     * @return Code object, newly created based on another Code (note: consider only returning ID or HTTP OK)
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Code cloneCode(Long codeId);

    /**
     * Delete a Code.
     * @param codeId ID of Code to delete
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void delete(Long codeId);

    /**
     * Get a List of Codes given a code String and code type.
     * @param code String code to search for
     * @param codeType Lookup type of code
     * @return List of Codes
     */
    List<Code> findAllByCodeAndType(String code, Lookup codeType);

    /**
     * Get a single Code given an ID.
     * @param codeId ID of Code to retrieve
     * @return Code object
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Code get(Long codeId) throws ResourceNotFoundException;

    /**
     * Get a Page of Code object, with pagination parameters (page, size of page etc) passed in as GetParameters object.
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size
     * of page etc
     * @return Page of Code objects
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN, RoleName.UNIT_ADMIN })
    Page<Code> getAllCodes(GetParameters getParameters);

    /**
     * Update an existing Code.
     * @param code Code object with updated properties
     * @return Code object, updated (note: consider only returning ID or HTTP OK)
     * @throws ResourceNotFoundException
     * @throws EntityExistsException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Code save(Code code) throws ResourceNotFoundException, EntityExistsException;

    /**
     * Get a list of Code corresponding to IBD Patient Management Diagnoses, currently stored in property
     * "patient.management.diagnoses.codes" with CD, UC IBDU
     * @return List of Code
     */
    List<Code> getPatientManagementDiagnoses();
}
