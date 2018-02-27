package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.model.BaseCode;
import org.patientview.api.service.CodeService;
import org.patientview.config.exception.ResourceInvalidException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeCategory;
import org.patientview.persistence.model.CodeExternalStandard;
import org.patientview.persistence.model.GetParameters;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import java.util.List;

/**
 * RESTful interface for the basic Crud operation for Codes (treatment and diagnosis).
 *
 * Created by jamesr@solidstategroup.com
 * Created on 26/06/2014
 */
@RestController
@ExcludeFromApiDoc
public class CodeController extends BaseController<CodeController> {

    @Inject
    private CodeService codeService;

    /**
     * Create a new Code.
     * @param code Code object containing all required properties
     * @return Code object, newly created (note: consider only returning ID or HTTP OK)
     * @throws ResourceInvalidException
     * @throws EntityExistsException
     */
    @RequestMapping(value = "/code", method = RequestMethod.POST
            , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Code> add(@RequestBody Code code) throws EntityExistsException, ResourceInvalidException {
        return new ResponseEntity<>(codeService.add(code), HttpStatus.CREATED);
    }

    /**
     * Add a new external standard String to a Code, creates new CodeExternalStandard associating Code with
     * ExternalStandard and sets a String codeString
     * @param codeId Long ID of Code
     * @param codeExternalStandard ExternalStandard object containing codeString and ID of ExternalStandard
     * @return Newly created CodeExternalStandard
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/code/{codeId}/externalstandards", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<CodeExternalStandard> addCodeExternalStandard(@PathVariable("codeId") Long codeId,
            @RequestBody CodeExternalStandard codeExternalStandard) throws ResourceNotFoundException {
        return new ResponseEntity<>(
                codeService.addCodeExternalStandard(codeId, codeExternalStandard), HttpStatus.CREATED);
    }

    /**
     * Associate a Code with a Category by creating a new CodeCategory object
     * @param codeId Long ID of Code
     * @param categoryId Long ID of Category
     * @return Newly created CodeCategory object
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/code/{codeId}/categories/{categoryId}", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<CodeCategory> addCodeCategory(@PathVariable("codeId") Long codeId,
            @PathVariable("categoryId") Long categoryId) throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.addCodeCategory(codeId, categoryId), HttpStatus.CREATED);
    }

    /**
     * Make a copy of an existing Code, typically to avoid having to re-enter large amounts of similar information in
     * UI.
     * @param codeId ID of Code to clone
     * @return Code object, newly created based on another Code (note: consider only returning ID or HTTP OK)
     */
    @RequestMapping(value = "/code/{codeId}/clone", method = RequestMethod.POST
            , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Code> cloneCode(@PathVariable("codeId") Long codeId) {
        return new ResponseEntity<>(codeService.cloneCode(codeId), HttpStatus.CREATED);
    }

    /**
     * Delete a Code.
     * @param codeId ID of Code to delete
     */
    @RequestMapping(value = "/code/{codeId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("codeId") Long codeId) {
        codeService.delete(codeId);
    }

    /**
     * Remove a Category from a Code by deleting a CodeCategory
     * @param codeId Long ID of Code
     * @param categoryId Long ID of Category
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/code/{codeId}/categories/{categoryId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteCodeCategory(@PathVariable("codeId") Long codeId, @PathVariable("categoryId") Long categoryId)
            throws ResourceNotFoundException {
        codeService.deleteCodeCategory(codeId, categoryId);
    }

    /**
     * Get a single Code given an ID.
     * @param codeId ID of Code to retrieve
     * @return Code object
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/code/{codeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Code> get(@PathVariable("codeId") Long codeId) throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.get(codeId), HttpStatus.OK);
    }

    /**
     * Get a Page of Code object, with pagination parameters (page, size of page etc) passed in as GetParameters object.
     * @param getParameters GetParameters object for pagination properties defined in UI, including page number, size
     * of page etc
     * @return Page of Code objects
     */
    @RequestMapping(value = "/code", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Page<Code>> getAllCodes(GetParameters getParameters) {
        return new ResponseEntity<>(codeService.getAllCodes(getParameters), HttpStatus.OK);
    }

    /**
     * Get all Code with CodeType of DIAGNOSIS.
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/code/diagnosis", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<BaseCode>> getAllDiagnosisCodes() throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.getAllDiagnosisCodes(), HttpStatus.OK);
    }

    /**
     * Get Codes with standard type PATIENTVIEW
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/codes/patientviewstandard", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<BaseCode>> getAllPatientViewStandardCodes()
            throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.getPatientViewStandardCodes(null), HttpStatus.OK);
    }

    /**
     * Get Codes given a Category id, DIAGNOSIS code type only
     * @param categoryId Long id of Category
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/codes/category/{categoryId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<BaseCode>> getByCategory(@PathVariable("categoryId") Long categoryId)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.getByCategory(categoryId), HttpStatus.OK);
    }

    /**
     * Get a single Code given an ID. Public use (e.g. choose condition)
     * @param codeId ID of Code to retrieve
     * @return BaseCode object
     */
    @RequestMapping(value = "/code/public/{codeId}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<BaseCode> getPublic(@PathVariable("codeId") Long codeId) {
        return new ResponseEntity<>(codeService.getPublic(codeId), HttpStatus.OK);
    }

    /**
     * Update an existing Code.
     * @param code Code object with updated properties
     * @return Code object, updated (note: consider only returning ID or HTTP OK)
     * @throws ResourceNotFoundException
     * @throws EntityExistsException
     */
    @RequestMapping(value = "/code", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Code> save(@RequestBody Code code) throws ResourceNotFoundException, EntityExistsException {
        return new ResponseEntity<>(codeService.save(code), HttpStatus.OK);
    }

    /**
     * Get diagnosis Codes by standard type and with a search term, used by patients when searching for conditions
     * @param searchTerm String term to search for
     * @param standardType String of standard type to search for
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/codes/diagnosis/{searchTerm}/standard/{standardType}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<BaseCode>> searchDiagnosisCodesByStandard(@PathVariable("searchTerm") String searchTerm,
                                                      @PathVariable("standardType") String standardType)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.searchDiagnosisCodes(searchTerm, standardType), HttpStatus.OK);
    }


    /**
     * Get diagnosis Codes by standard type and with a search term, used by patients when searching for conditions
     * @param searchTerm String term to search for
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/codes/diagnosis/{searchTerm}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<BaseCode>> searchDiagnosisCodes(@PathVariable("searchTerm") String searchTerm)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.searchAdminDiagnosisCodes(searchTerm), HttpStatus.OK);
    }


    /**
     * Get treatment Codes by standard type and with a search term, used by admin when searching for treatments
     * @param searchTerm String term to search for
     * @return List of BaseCode
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/codes/treatment/{searchTerm}", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<BaseCode>> searchTreatmentCodes(@PathVariable("searchTerm") String searchTerm)
            throws ResourceNotFoundException {
        return new ResponseEntity<>(codeService.searchTreatmentCodes(searchTerm), HttpStatus.OK);
    }




}
