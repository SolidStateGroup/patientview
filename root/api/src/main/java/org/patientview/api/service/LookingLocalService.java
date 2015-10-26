package org.patientview.api.service;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Looking Local utility service.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 20/10/2015
 */
public interface LookingLocalService {

    String getAuthErrorXml() throws TransformerException, IOException, ParserConfigurationException;

    String getErrorXml(String errorText) throws TransformerException, IOException, ParserConfigurationException;

    String getHomeXml() throws TransformerException, IOException, ParserConfigurationException;

    String getLoginSuccessfulXml(String token) throws TransformerException, IOException, ParserConfigurationException;

    String getMainXml(String token) throws TransformerException, IOException, ParserConfigurationException;

    String getMyDetailsXml(String token, int page)
            throws TransformerException, IOException, ParserConfigurationException;

    String getResultsXml(String token, int page) throws TransformerException, IOException, ParserConfigurationException;

    String getDrugsXml(String token, int page) throws TransformerException, IOException, ParserConfigurationException;

    String getLettersXml(String token, int page) throws TransformerException, IOException, ParserConfigurationException;
}
