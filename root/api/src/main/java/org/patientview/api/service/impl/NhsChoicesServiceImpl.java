package org.patientview.api.service.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.utilities.xml.NamespaceContextMap;
import org.patientview.api.service.NhsChoicesService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.NodeList;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/01/2016
 */
@Service
public class NhsChoicesServiceImpl extends AbstractServiceImpl<NhsChoicesServiceImpl> implements NhsChoicesService {

    private Abdera abdera;

    private DocumentBuilderFactory documentBuilderFactory;

    @Inject
    private Properties properties;

    @Override
    public Map<String, String> getDetailsByPracticeCode(String practiceCode) {
        if (StringUtils.isEmpty(practiceCode)) {
            return null;
        }

        try {
            Map<String, String> details = new HashMap<>();
            String apiKey = properties.getProperty("nhschoices.api.key");

            // get organisation ID from NHS choices
            URL practiceUrl = new URL("http://v1.syndication.nhschoices.nhs.uk/organisations/gppractices/odscode/"
                    + practiceCode
                    + ".json?apikey="
                    + apiKey);

            // get JSON for organisation from NHS choices
            JsonParser jp = new JsonParser();
            JsonElement root = jp.parse(IOUtils.toString(practiceUrl));
            JsonObject rootobj = root.getAsJsonObject();

            // must be an object
            if (!rootobj.isJsonObject()) {
                return null;
            }

            // must have organisation id
            JsonElement organisationIdObj = rootobj.get("OrganisationId");
            if (organisationIdObj == null) {
                return null;
            }

            String organisationId = organisationIdObj.getAsString();
            if (StringUtils.isEmpty(organisationId)) {
                return null;
            }

            JsonElement telephoneObj = rootobj.get("Telephone");
            if (telephoneObj != null) {
                String telephone = telephoneObj.getAsString();
                if (StringUtils.isNotEmpty(telephone)) {
                    details.put("telephone", telephone);
                }
            }

            // generate overview URL from found organisationId
            URL overviewUrl = new URL(
                "http://v1.syndication.nhschoices.nhs.uk/organisations/gppractices/"
                        + organisationId
                        + "/overview.xml?apikey="
                        + apiKey);

            if (abdera == null) {
                abdera = new Abdera();
            }

            if (documentBuilderFactory == null) {
                documentBuilderFactory = DocumentBuilderFactory.newInstance();
            }

            // try and read overview XML (atom format)
            Parser parser = abdera.getParser();
            Document<Feed> doc = parser.parse(overviewUrl.openStream(), overviewUrl.toString());
            Feed feed = doc.getRoot();
            if (CollectionUtils.isEmpty(feed.getEntries())) {
                return null;
            }

            // NHS choices url stored in first entry under alternate link
            Entry firstEntry = feed.getEntries().get(0);
            Link link = firstEntry.getAlternateLink();
            if (link != null) {
                IRI iri = link.getHref();
                if (iri != null) {
                    details.put("url", iri.toString());
                }
            }

            // return full path
            return details;
        } catch (IOException | ParseException e) {
            LOG.info("Could not retrieve overview url from NHS choices for practice with code "
                    + practiceCode + ", continuing");
        }

        return null;
    }

    // testing only
    @Override
    public void updateOrganisations()
            throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        String apiKey = properties.getProperty("nhschoices.api.key");
        String urlString
        //    = "http://v1.syndication.nhschoices.nhs.uk/organisations/gppractices/14500/overview.xml?apikey=" + apiKey;
            = "http://v1.syndication.nhschoices.nhs.uk/organisations/gppractices/postcode/W67HY.xml?range=1&apikey="
                + apiKey;

        System.out.println("url: " + urlString);

        if (abdera == null) {
            abdera = new Abdera();
        }

        if (documentBuilderFactory == null) {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
        }

        Parser parser = abdera.getParser();

        URL url = new URL(urlString);
        Document<Feed> doc = parser.parse(url.openStream(),url.toString());
        Feed feed = doc.getRoot();

        for (Entry entry : feed.getEntries()) {
            System.out.println("link: " + entry.getLink("alternate").getHref());

            org.w3c.dom.Document content = documentBuilderFactory.newDocumentBuilder().parse(
                    new InputSource(new StringReader(entry.getContent())));
            NamespaceContext context = new NamespaceContextMap("s", "http://syndication.nhschoices.nhs.uk/services");
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(context);

            //String base = "/s:overview";
            String base = "s:organisationSummary";

            String name = (String) xpath.compile(
                    base + "/s:name").evaluate(content, XPathConstants.STRING);
            String odsCode = (String) xpath.compile(
                    base + "/s:odsCode").evaluate(content, XPathConstants.STRING);
            NodeList addressLines = (NodeList) xpath.compile(
                    base + "/s:address/s:addressLine").evaluate(content, XPathConstants.NODESET);
            String postcode = (String) xpath.compile(
                    base + "/s:address/s:postcode").evaluate(content, XPathConstants.STRING);
            String telephone = (String) xpath.compile(
                    base + "/s:contact[1]/s:telephone").evaluate(content, XPathConstants.STRING);

            System.out.println("name: " + name);
            System.out.println("ods code: " + odsCode);

            for (int i = 0; i < addressLines.getLength(); i++) {
                System.out.println("address line " + i + ": " + addressLines.item(i).getTextContent());
            }

            System.out.println("postcode: " + postcode);
            System.out.println("telephone: " + telephone);
            System.out.println("");
        }
    }
}
