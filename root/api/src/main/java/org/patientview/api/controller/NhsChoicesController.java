package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.NhsChoicesService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * RESTful interface for retrieving data from NHS choices e.g.
 * http://v1.syndication.nhschoices.nhs.uk/organisations/gppractices/
 *
 * Created by jamesr@solidstategroup.com
 * Created on 18/01/2016
 */
@ExcludeFromApiDoc
@RestController
public class NhsChoicesController extends BaseController<NhsChoicesController> {

    @Inject
    private NhsChoicesService nhsChoicesService;

    @RequestMapping(value = "/nhschoices/organisations/update", method = RequestMethod.POST)
    @ResponseBody
    public void updateOrganisations() {
        nhsChoicesService.updateOrganisations();
    }
}
