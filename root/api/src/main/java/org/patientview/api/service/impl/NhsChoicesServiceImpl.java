package org.patientview.api.service.impl;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.Parser;
import org.hl7.fhir.utilities.xml.NamespaceContextMap;
import org.patientview.api.service.NhsChoicesService;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 15/07/2014
 */
@Service
public class NhsChoicesServiceImpl extends AbstractServiceImpl<NhsChoicesServiceImpl> implements NhsChoicesService {

    @Inject
    Properties properties;

    public void updateOrganisations()
            throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        String apiKey = properties.getProperty("nhschoices.api.key");
        String urlString
            = "http://v1.syndication.nhschoices.nhs.uk/organisations/gppractices/14500/overview.xml?apikey=" + apiKey;

        System.out.println("url: " + urlString);

        Abdera abdera = new Abdera();
        Parser parser = abdera.getParser();

        URL url = new URL(urlString);
        Document<Feed> doc = parser.parse(url.openStream(),url.toString());
        Feed feed = doc.getRoot();

        for (Entry entry : feed.getEntries()) {
            org.w3c.dom.Document content = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(
                    new InputSource(new StringReader(entry.getContent())));
            NamespaceContext context = new NamespaceContextMap("s", "http://syndication.nhschoices.nhs.uk/services");
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(context);

            String name = (String) xpath.compile("/s:overview/s:name").evaluate(content, XPathConstants.STRING);
            String odsCode = (String) xpath.compile("/s:overview/s:odsCode").evaluate(content, XPathConstants.STRING);

            System.out.println("name: " + name);
            System.out.println("ods code: " + odsCode);
        }
    }
}
