package org.patientview.api.controller;

import org.patientview.api.config.ExcludeFromApiDoc;
import org.patientview.api.service.NhsChoicesService;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;

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

    @RequestMapping(value = "/nhschoices/conditions/update", method = RequestMethod.POST)
    @ResponseBody
    public void updateConditions() throws ImportResourceException {
        nhsChoicesService.updateConditions();
    }

    @RequestMapping(value = "/nhschoices/conditions/synchronise", method = RequestMethod.POST)
    @ResponseBody
    public void synchroniseConditions() throws ResourceNotFoundException {
        nhsChoicesService.synchroniseConditions();
    }

    @RequestMapping(value = "/nhschoices/conditions/categorise", method = RequestMethod.POST)
    @ResponseBody
    public void categoriseConditions() throws ResourceNotFoundException {
        nhsChoicesService.categoriseConditions();
    }

    @RequestMapping(value = "/nhschoices/organisations/update", method = RequestMethod.POST)
    @ResponseBody
    public void updateOrganisations()
            throws ParserConfigurationException, SAXException, XPathExpressionException, IOException {
        nhsChoicesService.updateOrganisations();
    }
}
