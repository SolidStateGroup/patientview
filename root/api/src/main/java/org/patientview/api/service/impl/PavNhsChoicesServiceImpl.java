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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hl7.fhir.utilities.xml.NamespaceContextMap;
import org.joda.time.DateTime;
import org.patientview.api.service.MedlinePlusService;
import org.patientview.api.service.NhsChoicesService;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Category;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.CodeCategory;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.NhschoicesCondition;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeSourceTypes;
import org.patientview.persistence.model.enums.CodeStandardTypes;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.LinkTypes;
import org.patientview.persistence.model.enums.LookupTypes;
import org.patientview.persistence.repository.CategoryRepository;
import org.patientview.persistence.repository.CodeCategoryRepository;
import org.patientview.persistence.repository.CodeRepository;
import org.patientview.persistence.repository.LookupRepository;
import org.patientview.persistence.repository.NhschoicesConditionRepository;
import org.patientview.persistence.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.persistence.NonUniqueResultException;
import javax.transaction.Transactional;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.RandomAccess;
import java.util.Set;

import static org.patientview.api.util.ApiUtil.getCurrentUser;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/01/2016
 */
@Service
public class PavNhsChoicesServiceImpl extends AbstractServiceImpl<PavNhsChoicesServiceImpl> {

    // https://abdera.apache.org/ - An Open Source Atom Implementation
    private Abdera abdera;
    private DocumentBuilderFactory documentBuilderFactory;


    @Inject
    private Properties properties;

    private String getConditionCodeFromUri(String uri) {
        return uri.split("/")[uri.split("/").length - 1];
    }

    // testing
    public void updateConditions() throws ImportResourceException {
        LOG.info("Updating NHS Choices conditions, from endpoint");
        updateConditionsWorker("A");
    }

    // testing only
    public void updateOrganisations()
            throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        String apiKey = properties.getProperty("nhschoices.api.key");
        String urlString
            //= "http://v1.syndication.nhschoices.nhs.uk/organisations/gppractices/14500/overview.xml?apikey=" + apiKey;
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
        Document<Feed> doc = parser.parse(url.openStream(), url.toString());
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


    private void updateConditionsWorker(String letter) throws ImportResourceException {
        String apiKey = properties.getProperty("nhschoices.api.key");
        //String urlString = "http://v1.syndication.nhschoices.nhs.uk/conditions/atoz.xml?apikey=" + apiKey;
        String urlString = "http://v1.syndication.nhschoices.nhs.uk/conditions/atoz.xml?apikey=" + apiKey;

        if (documentBuilderFactory == null) {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
        }

        DocumentBuilder documentBuilder;

        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            throw new ImportResourceException("Error building DocumentBuilder");
        }

        // get alphabetical listing of urls for conditions
        org.w3c.dom.Document doc;

        try {
            doc = documentBuilder.parse(new URL(urlString).openStream());
        } catch (SAXException | IOException e) {
            throw new ImportResourceException("Error reading alphabetical listing of NHS Choices conditions: "
                    + urlString);
        }

        List<String> aToZPages = new ArrayList<>();

        for (Node node : XmlUtil.asList(doc.getDocumentElement().getElementsByTagName("Link"))) {
            for (Node childNode : XmlUtil.asList(node.getChildNodes())) {
                if (childNode.getNodeName().equals("Uri")) {
                    aToZPages.add(childNode.getTextContent().replace("?apikey", ".xml?apikey"));
                }
            }
        }

        // now have list of all a-z and 0-9 pages with conditions lists in xml representation
        Map<String, NhschoicesCondition> newUriMap = new HashMap<>();

        for (String pageUrl : aToZPages) {
            LOG.info("Updating NhschoicesConditions from page: " + pageUrl);

            try {
                doc = documentBuilder.parse(new URL(pageUrl).openStream());
            } catch (SAXException | IOException e) {
                throw new ImportResourceException("Error reading page of conditions: " + pageUrl);
            }

            for (Node node : XmlUtil.asList(doc.getDocumentElement().getElementsByTagName("Link"))) {
                String text = null;
                String uri = null;

                for (Node childNode : XmlUtil.asList(node.getChildNodes())) {
                    if (childNode.getNodeName().equals("Text")) {
                        text = childNode.getTextContent();
                    }
                    if (childNode.getNodeName().equals("Uri")) {
                        // handle broken links from nhs choices as produce .aspx error page
                        if (!childNode.getTextContent().contains(".aspx")) {
                            uri = childNode.getTextContent().split("\\?apikey")[0];
                        }
                    }
                }

                if (text != null && uri != null) {
                    newUriMap.put(uri, new NhschoicesCondition(text, uri));
                }
            }

            // sleep for 3 seconds to avoid 3.1.xiv in nhs-choices-standard-license-terms.pdf
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ie) {
                throw new ImportResourceException("Thread interrupted");
            }
        }

        // compare to existing using uri, adding if required with correct details
//        List<NhschoicesCondition> currentConditions = nhschoicesConditionRepository.findAll();
//        List<String> currentConditionCodes = new ArrayList<>();
//        Map<String, NhschoicesCondition> currentUriMap = new HashMap<>();
//        Map<String, NhschoicesCondition> currentCodeMap = new HashMap<>();
//
//        for (NhschoicesCondition currentCondition : currentConditions) {
//            currentUriMap.put(currentCondition.getUri(), currentCondition);
//            currentConditionCodes.add(currentCondition.getCode());
//            currentCodeMap.put(currentCondition.getCode(), currentCondition);
//        }

        // set creator to importer if not called by a user from endpoint (e.g. run as task)
//        User currentUser = getCurrentUser();
//        if (currentUser == null) {
//            currentUser = userRepository.findByUsernameCaseInsensitive("importer");
//        }

        List<String> newConditionCodes = new ArrayList<>();

//        for (String uri : newUriMap.keySet()) {
//            String conditionCode = getConditionCodeFromUri(uri);
//            newConditionCodes.add(conditionCode);
//
//            if (!currentUriMap.keySet().contains(uri)) {
//                // new entry, introduction url and description are set on get Code to avoid limit
//                NhschoicesCondition newCondition = newUriMap.get(uri);
//                newCondition.setCreator(currentUser);
//                newCondition.setCreated(new Date());
//                newCondition.setCode(conditionCode);
//                newCondition.setLastUpdate(newCondition.getCreated());
//                newCondition.setLastUpdater(currentUser);
//                nhschoicesConditionRepository.save(newCondition);
//            } else {
//                // existing entry, clear dates for introduction url and description so updated on next get Code
//                NhschoicesCondition existingCondition = currentCodeMap.get(conditionCode);
//                if (existingCondition != null) {
//                    existingCondition.setIntroductionUrlLastUpdateDate(null);
//                    existingCondition.setDescriptionLastUpdateDate(null);
//                    existingCondition.setLastUpdate(new Date());
//                    existingCondition.setLastUpdater(currentUser);
//                    nhschoicesConditionRepository.save(existingCondition);
//                }
//            }
//        }

        // delete old NhschoiceCondition, no longer on NHS Choices
//        currentConditionCodes.removeAll(newConditionCodes);

//        if (!currentConditionCodes.isEmpty()) {
//            nhschoicesConditionRepository.deleteByCode(currentConditionCodes);
//        }
    }
    private static final class XmlUtil {
        private XmlUtil() {
        }

        public static List<Node> asList(NodeList n) {
            return n.getLength() == 0 ? Collections.<Node>emptyList() : new NodeListWrapper(n);
        }

        static final class NodeListWrapper extends AbstractList<Node> implements RandomAccess {
            private final NodeList list;

            NodeListWrapper(NodeList l) {
                list = l;
            }

            public Node get(int index) {
                return list.item(index);
            }

            public int size() {
                return list.getLength();
            }
        }
    }
}
