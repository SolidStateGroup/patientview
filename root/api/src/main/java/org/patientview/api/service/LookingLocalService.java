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

    String getHomeXml() throws ParserConfigurationException, TransformerException, IOException;
}
