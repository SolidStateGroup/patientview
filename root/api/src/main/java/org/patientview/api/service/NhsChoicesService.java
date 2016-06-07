package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Map;

/**
 * NHS Choices service, for retrieving data from NHS Choices
 *
 * Created by jamesr@solidstategroup.com
 * Created on 18/01/2016
 */
public interface NhsChoicesService {
    Code setDescription(String code) throws ResourceNotFoundException, ImportResourceException;

    Map<String, String> getDetailsByPracticeCode(String practiceCode);

    void setIntroductionUrl(String code) throws ResourceNotFoundException, ImportResourceException;

    void synchroniseConditions() throws ResourceNotFoundException;

    //@RoleOnly
    void updateConditions() throws ImportResourceException;

    // testing only
    @RoleOnly
    void updateOrganisations() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException;
}
