package org.patientview.api.service;

import org.patientview.api.annotation.RoleOnly;
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

    @RoleOnly
    void updateConditions() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException;

    Map<String, String> getDetailsByPracticeCode(String practiceCode);

    // testing only
    @RoleOnly
    void updateOrganisations() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException;
}
