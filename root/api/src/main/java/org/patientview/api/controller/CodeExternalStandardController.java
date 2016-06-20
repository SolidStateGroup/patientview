package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.CodeService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.CodeExternalStandard;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 08/06/2016
 */
@RestController
@ExcludeFromApiDoc
public class CodeExternalStandardController extends BaseController<CodeExternalStandardController> {

    @Inject
    private CodeService codeService;

    /**
     * Delete a CodeExternalStandard, removing from the associated Code
     * @param codeExternalStandardId Long ID of CodeExternalStandard
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/codeexternalstandards/{codeExternalStandardId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void delete(@PathVariable("codeExternalStandardId") Long codeExternalStandardId)
            throws ResourceNotFoundException {
        codeService.deleteCodeExternalStandard(codeExternalStandardId);
    }

    /**
     * Update an existing CodeExternalStandard, setting the codeString and ExternalStandard
     * @param codeExternalStandard CodeExternalStandard, containing updated codeString and ExternalStandard
     * @throws ResourceNotFoundException
     */
    @RequestMapping(value = "/codeexternalstandards", method = RequestMethod.PUT)
    @ResponseBody
    public void save(@RequestBody CodeExternalStandard codeExternalStandard) throws ResourceNotFoundException {
        codeService.saveCodeExternalStandard(codeExternalStandard);
    }
}
