package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.MedlinePlusService;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * Admin RESTful interface for managing MadlinePlus services
 */
@ExcludeFromApiDoc
@RestController
public class MedlinePlusController extends BaseController<MedlinePlusController> {

    @Inject
    private MedlinePlusService medlinePlusService;

    /**
     * Reads ID-10 codes from excel sheet and maps them to one of the NHS choices codes
     * and creates CodeExternalStandard and code external link
     *
     * @throws ImportResourceException
     */
    @RequestMapping(value = "/medlineplus/codes/sync", method = RequestMethod.POST)
    @ResponseBody
    public void syncCodes() throws ResourceNotFoundException, ImportResourceException {
        medlinePlusService.syncICD10Codes();
    }
}
