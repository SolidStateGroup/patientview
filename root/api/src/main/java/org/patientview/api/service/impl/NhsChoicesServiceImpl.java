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
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.NhschoicesCondition;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeTypes;
import org.patientview.persistence.model.enums.LookupTypes;
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
import javax.transaction.Transactional;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.RandomAccess;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/01/2016
 */
@Service
public class NhsChoicesServiceImpl extends AbstractServiceImpl<NhsChoicesServiceImpl> implements NhsChoicesService {

    // https://abdera.apache.org/ - An Open Source Atom Implementation
    private Abdera abdera;

    @Inject
    private CodeRepository codeRepository;

    private DocumentBuilderFactory documentBuilderFactory;

    @Inject
    private LookupRepository lookupRepository;

    @Inject
    private NhschoicesConditionRepository nhschoicesConditionRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private Properties properties;

    private String getConditionCodeFromUri(String uri) {
        return uri.split("/")[uri.split("/").length - 1];
    }

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

    private Integer getUrlStatus(String url) {
        try {
            HttpURLConnection huc = (HttpURLConnection) new URL(url).openConnection();
            huc.setRequestMethod("HEAD");  //OR  huc.setRequestMethod ("GET");
            huc.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; " +
                    ".NET CLR 1.0.3705; .NET CLR 1.1.4322; .NET CLR 1.2.30703)");
            huc.connect();
            return huc.getResponseCode();
        } catch (Exception e) {
            return null;
        }
    }

    private void setConditionIntroductionUrl(NhschoicesCondition condition) {
        String introductionUrl = "http://www.nhs.uk/conditions/" + condition.getCode() + "/Pages/Introduction.aspx";
        String definitionUrl = "http://www.nhs.uk/conditions/" + condition.getCode() + "/Pages/Definition.aspx";

        Integer status = getUrlStatus(introductionUrl);

        if (status != null && status.equals(200)) {
            // as expected
            condition.setIntroductionUrl(introductionUrl);
            condition.setIntroductionUrlStatus(status);
        } else {
            status = getUrlStatus(definitionUrl);
            if (status != null && status.equals(200)) {
                // as expected with alternate url
                condition.setIntroductionUrl(definitionUrl);
                condition.setIntroductionUrlStatus(status);
            } else {
                condition.setIntroductionUrlStatus(status);
            }
        }
    }

    @Override
    @Transactional
    public void synchroniseConditions() throws ResourceNotFoundException {
        // synchronise conditions previously retrieved from nhs choices, may be consolidated into once function call
        Lookup standardType = lookupRepository.findByTypeAndValue(LookupTypes.CODE_STANDARD, "PATIENTVIEW");
        if (standardType == null) {
            throw new ResourceNotFoundException("Could not find PATIENTVIEW code standard type Lookup");
        }
        Lookup codeType = lookupRepository.findByTypeAndValue(LookupTypes.CODE_TYPE, CodeTypes.DIAGNOSIS.toString());
        if (codeType == null) {
            throw new ResourceNotFoundException("Could not find DIAGNOSIS code type Lookup");
        }

        // set creator to importer if not called by a user from endpoint (e.g. run as task)
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            currentUser = userRepository.findByUsernameCaseInsensitive("importer");
        }

        // get codes and conditions to synchronise
        List<Code> codes = codeRepository.findAllByStandardType(standardType);
        List<NhschoicesCondition> conditions = nhschoicesConditionRepository.findAll();

        Map<String, Code> codesMap = new HashMap<>();

        for (Code code : codes) {
            codesMap.put(code.getCode(), code);
        }

        List<Code> newCodes = new ArrayList<>();

        for (NhschoicesCondition condition : conditions) {
            if (codesMap.keySet().contains(condition.getCode())) {
                // exists in patientview already
            } else {
                // is new and must be converted and saved
                Code code = new Code();
                code.setCreator(currentUser);
                code.setCreated(new Date());
                code.setLastUpdater(currentUser);
                code.setLastUpdate(new Date());
                code.setCode(condition.getCode());
                code.setCodeType(codeType);
                code.setStandardType(standardType);
                code.setDescription(condition.getName());
                newCodes.add(code);
            }
        }

        codeRepository.save(newCodes);
    }

    // helper to convert NodeList to List of Nodes
    @Override
    @Transactional
    public void updateConditions()
            throws ParserConfigurationException, IOException, SAXException, XPathExpressionException {
        String apiKey = properties.getProperty("nhschoices.api.key");
        String urlString = "http://v1.syndication.nhschoices.nhs.uk/conditions/atoz.xml?apikey=" + apiKey;

        if (documentBuilderFactory == null) {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
        }

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        // get alphabetical listing of urls for conditions
        org.w3c.dom.Document doc = documentBuilder.parse(new URL(urlString).openStream());
        List<String> aToZPages = new ArrayList<>();

        for (Node node : XmlUtil.asList(doc.getDocumentElement().getElementsByTagName("Link"))) {
            for (Node childNode : XmlUtil.asList(node.getChildNodes())) {
                if (childNode.getNodeName().equals("Uri")) {
                    aToZPages.add(childNode.getTextContent().replace("?apikey", ".xml?apikey"));
                }
            }
        }

        // now have list of all a-z and 0-9 pages with conditions lists in xml representation
        Map<String, NhschoicesCondition> uriMap = new HashMap<>();

        for (String pageUrl : aToZPages) {
            doc = documentBuilder.parse(new URL(pageUrl).openStream());

            for (Node node : XmlUtil.asList(doc.getDocumentElement().getElementsByTagName("Link"))) {
                String text = null;
                String uri = null;

                for (Node childNode : XmlUtil.asList(node.getChildNodes())) {
                    if (childNode.getNodeName().equals("Text")) {
                        text = childNode.getTextContent();
                    }
                    if (childNode.getNodeName().equals("Uri")) {
                        // handle broken links from nhs choices as produce .aspx error page
                        //if (!childNode.getTextContent().contains(".aspx")) {
                        uri = childNode.getTextContent().split("\\?apikey")[0];
                        //}
                    }
                }

                if (text != null && uri != null) {
                    uriMap.put(uri, new NhschoicesCondition(text, uri));
                }
            }
        }

        // compare to existing using uri, adding if required with correct details
        List<NhschoicesCondition> currentConditions = nhschoicesConditionRepository.findAll();
        Map<String, NhschoicesCondition> currentUriMap = new HashMap<>();

        for (NhschoicesCondition currentCondition : currentConditions) {
            currentUriMap.put(currentCondition.getUri(), currentCondition);
        }

        // set creator to importer if not called by a user from endpoint (e.g. run as task)
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            currentUser = userRepository.findByUsernameCaseInsensitive("importer");
        }

        for (String uri : uriMap.keySet()) {
            if (!currentUriMap.keySet().contains(uri)) {
                // new entry
                NhschoicesCondition newCondition = uriMap.get(uri);
                newCondition.setCreator(currentUser);
                newCondition.setCreated(new Date());
                newCondition.setCode(getConditionCodeFromUri(uri));

                // set introduction url and status (note: request goes from 3s to 60s, IP blocked after 3 runs)
                //setConditionIntroductionUrl(newCondition);
                newCondition.setIntroductionUrl("http://www.nhs.uk/conditions/" + newCondition.getCode()
                        + "/Pages/Introduction.aspx");

                nhschoicesConditionRepository.save(newCondition);
            }
        }
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

    private static final class XmlUtil {
        private XmlUtil() {}

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
