package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.api.model.BaseCode;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeCategory;
import org.patientview.persistence.model.CodeExternalStandard;
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
public interface CodeService {

    /**
     * Create a new Code.
     * @param code Code object containing all required properties
     * @return Code object, newly created (note: consider only returning ID or HTTP OK)
     * @throws ResourceInvalidException
     * @throws EntityExistsException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    Code add(Code code) throws EntityExistsException, ResourceInvalidException;

    /**
     * Associate a Code with a Category by creating a new CodeCategory object
     * @param codeId Long ID of Code
     * @param categoryId Long ID of Category
     * @return Newly created CodeCategory object
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    CodeCategory addCodeCategory(Long codeId, Long categoryId) throws ResourceNotFoundException;

    /**
     * Add a new external standard String to a Code, creates new CodeExternalStandard associating Code with
     * ExternalStandard and sets a String codeString
     * @param codeId Long ID of Code
     * @param codeExternalStandard ExternalStandard object containing codeString and ID of ExternalStandard
     * @return Newly created CodeExternalStandard
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    CodeExternalStandard addCodeExternalStandard(Long codeId, CodeExternalStandard codeExternalStandard)
            throws ResourceNotFoundException;

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
     * Remove a Category from a Code by deleting a CodeCategory
     * @param codeId Long ID of Code
     * @param categoryId Long ID of Category
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteCodeCategory(Long codeId, Long categoryId) throws ResourceNotFoundException;

    /**
     * Delete a CodeExternalStandard, removing from the associated Code
     * @param codeExternalStandardId Long ID of CodeExternalStandard
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void deleteCodeExternalStandard(Long codeExternalStandardId) throws ResourceNotFoundException;

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
     * Get all Code with CodeType of DIAGNOSIS.
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    List<BaseCode> getAllDiagnosisCodes() throws ResourceNotFoundException;

    /**
     * Get Codes given a Category id
     * @param categoryId Long id of Category
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    List<BaseCode> getByCategory(Long categoryId) throws ResourceNotFoundException;

    /**
     * Get a single Code by String code
     * @param code String code of Code to get
     * @return Code object
     */
    Code getByCode(String code);

    /**
     * Get a list of Code corresponding to IBD Patient Management Diagnoses, currently stored in property
     * "patient.management.diagnoses.codes" with CD, UC IBDU
     * @return List of Code
     */
    List<Code> getPatientManagementDiagnoses();

    /**
     * Get Codes with standard type PATIENTVIEW
     * @param searchTerm Optional String search term
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.PATIENT })
    List<BaseCode> getPatientViewStandardCodes(String searchTerm) throws ResourceNotFoundException;

    /**
     * Get a single Code given an ID. Used by patients when adding conditions
     * @param codeId ID of Code to retrieve
     * @return Code object
     */
    @RoleOnly(roles = { RoleName.PATIENT })
    BaseCode getPublic(Long codeId);

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
     * Update an existing CodeExternalStandard, setting the codeString and ExternalStandard
     * @param codeExternalStandard CodeExternalStandard, containing updated codeString and ExternalStandard
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.SPECIALTY_ADMIN })
    void saveCodeExternalStandard(CodeExternalStandard codeExternalStandard) throws ResourceNotFoundException;

    /**
     * Get diagnosis Codes by standard type and with a search term, used by patients when searching for conditions
     * @param searchTerm String term to search for
     * @param standardType String of standard type to search for
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.PATIENT })
    List<BaseCode> searchDiagnosisCodes(String searchTerm, String standardType) throws ResourceNotFoundException;

    /**
     * Get diagnosis Codes with a search term, used by admins when searching for diagnosis
     * @param searchTerm String term to search for
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    List<BaseCode> searchAdminDiagnosisCodes(String searchTerm) throws ResourceNotFoundException;

    /**
     * Get diagnosis Codes by standard type and with a search term, used by patients when searching for conditions
     * @param searchTerm String term to search for
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    @RoleOnly(roles = { RoleName.PATIENT })
    List<BaseCode> searchTreatmentCodes(String searchTerm) throws ResourceNotFoundException;
}
