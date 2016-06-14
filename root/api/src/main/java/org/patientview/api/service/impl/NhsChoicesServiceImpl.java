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
import org.joda.time.DateTime;
import org.patientview.api.service.NhsChoicesService;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Code;
import org.patientview.persistence.model.Lookup;
import org.patientview.persistence.model.NhschoicesCondition;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.CodeSourceTypes;
import org.patientview.persistence.model.enums.CodeStandardTypes;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.RandomAccess;
import java.util.Set;

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

    private static final String NHS_CHOICES_LINK_DESCRIPTION = "NHS Choices Information";

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

    @Override
    public Code setDescription(String code) throws ResourceNotFoundException, ImportResourceException {
        NhschoicesCondition condition = nhschoicesConditionRepository.findOneByCode(code);

        if (condition != null) {
            // get Code
            Code entityCode = codeRepository.findOneByCode(code);
            if (entityCode == null) {
                throw new ResourceNotFoundException("Could not find Code with code '" + code + "'");
            }

            Date oneMonthAgo = new DateTime(new Date()).minusMonths(1).toDate();
            Date now = new Date();

            // check if Code already has a description
            if (StringUtils.isEmpty(entityCode.getFullDescription())) {
                // description is empty, check if NHS Choices Condition description has been updated in the last month
                if (condition.getDescriptionLastUpdateDate() == null
                        || condition.getDescriptionLastUpdateDate().before(oneMonthAgo)) {
                    // extract introduction text from NHS Choices and store in Condition and Code, uses standard url
                    // format for introduction
                    String urlString = "http://v1.syndication.nhschoices.nhs.uk/conditions/articles/" + code
                            + "/introduction.xml?apikey=" + properties.getProperty("nhschoices.api.key");

                    LOG.info("Updating '" + code + "' with description from " + urlString);

                    // atom XML format
                    if (abdera == null) {
                        abdera = new Abdera();
                    }

                    if (documentBuilderFactory == null) {
                        documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    }

                    Parser parser = abdera.getParser();

                    // try and retrieve XML representation from NHS Choices
                    // sleep for 3 seconds to avoid 3.1.xiv in nhs-choices-standard-license-terms.pdf
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        throw new ImportResourceException("Thread interrupted");
                    }

                    URL url;

                    try {
                        url = new URL(urlString);
                    } catch (IOException e) {
                        throw new ImportResourceException("IOException creating URL for " + urlString);
                    }

                    Document<Feed> doc;

                    try {
                        doc = parser.parse(url.openStream(), url.toString());
                        Feed feed = doc.getRoot();

                        // get first entry
                        if (!CollectionUtils.isEmpty(feed.getEntries())) {
                            // try to set from summary (long description)
                            String summary = feed.getEntries().get(0).getSummary();

                            if (StringUtils.isNotEmpty(summary)) {
                                // save NHS Choices Condition
                                condition.setDescription(summary);
                                condition.setDescriptionLastUpdateDate(now);
                                condition.setLastUpdate(now);
                                nhschoicesConditionRepository.save(condition);

                                // save Code
                                entityCode.setFullDescription(summary);
                                entityCode.setLastUpdate(now);
                                codeRepository.save(entityCode);

                                return entityCode;
                            }
                        }
                    } catch (FileNotFoundException e) {
                        // manually catch 404 errors from server
                        LOG.info("404 error updating '" + code + "' with description from " + urlString);

                        // remove condition description
                        condition.setDescription(null);
                        condition.setDescriptionLastUpdateDate(now);
                        condition.setLastUpdate(now);
                        nhschoicesConditionRepository.save(condition);

                        // remove Code full description
                        entityCode.setFullDescription(null);
                        entityCode.setLastUpdate(now);
                        codeRepository.save(entityCode);

                        return entityCode;
                    } catch (IOException e) {
                        // manually catch errors from server (workaround for API 404 reported as 500)
                        if (e.getMessage().contains("500")) {
                            // API can return 500 instead of 404 if not found
                            LOG.info("500 error updating '" + code + "' with description from " + urlString + ": "
                                + e.getMessage());
                        } else {
                            // not 404 or 500 error, could be 403
                            LOG.info("Error updating '" + code + "' with description from " + urlString + ": "
                                    + e.getMessage());
                        }

                        // remove condition description
                        condition.setDescription(null);
                        condition.setDescriptionLastUpdateDate(now);
                        condition.setLastUpdate(now);
                        nhschoicesConditionRepository.save(condition);

                        // remove Code full description
                        entityCode.setFullDescription(null);
                        entityCode.setLastUpdate(now);
                        codeRepository.save(entityCode);

                        return entityCode;
                    } catch (ParseException e) {
                        // error with abdera parsing, remove condition description
                        condition.setDescription(null);
                        condition.setDescriptionLastUpdateDate(now);
                        condition.setLastUpdate(now);
                        nhschoicesConditionRepository.save(condition);

                        // remove Code full description
                        entityCode.setFullDescription(null);
                        entityCode.setLastUpdate(now);
                        codeRepository.save(entityCode);

                        return entityCode;
                    }
                }
            }
        }

        // Code not updated
        return null;
    }

    @Override
    @Transactional
    public void setIntroductionUrl(String code) throws ResourceNotFoundException, ImportResourceException {
        NhschoicesCondition condition = nhschoicesConditionRepository.findOneByCode(code);

        Date oneMonthAgo = new DateTime(new Date()).minusMonths(1).toDate();
        Date now = new Date();

        // if NHS Choices Condition is found and not already set to status 200 and not updated in last month
        if (condition != null &&
                (condition.getIntroductionUrlStatus() == null || !condition.getIntroductionUrlStatus().equals(200))
                && (condition.getIntroductionUrlLastUpdateDate() == null
                    || condition.getIntroductionUrlLastUpdateDate().before(oneMonthAgo))) {
            // check against both urls
            String introductionUrl = "http://www.nhs.uk/conditions/" + condition.getCode() + "/Pages/Introduction.aspx";
            String definitionUrl = "http://www.nhs.uk/conditions/" + condition.getCode() + "/Pages/Definition.aspx";

            LOG.info("Updating '" + code + "' with introduction url " + introductionUrl);

            Integer status = getUrlStatus(introductionUrl);
            boolean foundIntroductionPage = false;

            if (status != null && status.equals(200)) {
                // as expected
                condition.setIntroductionUrl(introductionUrl);
                condition.setIntroductionUrlStatus(status);
                condition.setIntroductionUrlLastUpdateDate(now);
                condition.setLastUpdate(now);
                foundIntroductionPage = true;
            } else {
                LOG.info("Updating '" + code + "' with introduction url " + definitionUrl);

                // sleep for 3 seconds to avoid 3.1.xiv in nhs-choices-standard-license-terms.pdf
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    throw new ImportResourceException("Thread interrupted");
                }

                status = getUrlStatus(definitionUrl);
                if (status != null && status.equals(200)) {
                    // as expected with alternate url
                    condition.setIntroductionUrl(definitionUrl);
                    condition.setIntroductionUrlStatus(status);
                    condition.setIntroductionUrlLastUpdateDate(now);
                    condition.setLastUpdate(now);
                    foundIntroductionPage = true;
                } else {
                    // 404, 403 or otherwise, remove introduction url
                    condition.setIntroductionUrl(null);
                    condition.setIntroductionUrlStatus(status);
                    condition.setIntroductionUrlLastUpdateDate(now);
                    condition.setLastUpdate(now);
                }
            }

            nhschoicesConditionRepository.save(condition);

            // update Code with link if does not exist
            Code entityCode = codeRepository.findOneByCode(code);
            if (entityCode == null) {
                throw new ResourceNotFoundException("Could not find Code with code '" + code + "'");
            }

            org.patientview.persistence.model.Link foundLink = null;

            // check Link exists already with NHS Choices description
            for (org.patientview.persistence.model.Link link : entityCode.getLinks()) {
                if (link.getName().equals(NHS_CHOICES_LINK_DESCRIPTION)) {
                    foundLink = link;
                }
            }

            if (foundLink == null && foundIntroductionPage) {
                // no existing link, introduction page exists, create new Link
                org.patientview.persistence.model.Link nhschoicesLink
                        = new org.patientview.persistence.model.Link();
                nhschoicesLink.setLink(condition.getIntroductionUrl());
                nhschoicesLink.setName(NHS_CHOICES_LINK_DESCRIPTION);
                nhschoicesLink.setCode(entityCode);
                nhschoicesLink.setCreator(getCurrentUser());
                nhschoicesLink.setCreated(now);
                nhschoicesLink.setLastUpdater(getCurrentUser());
                nhschoicesLink.setLastUpdate(nhschoicesLink.getCreated());

                if (entityCode.getLinks().isEmpty()) {
                    nhschoicesLink.setDisplayOrder(1);
                } else {
                    nhschoicesLink.setDisplayOrder(entityCode.getLinks().size() + 1);
                }

                entityCode.getLinks().add(nhschoicesLink);
                entityCode.setLastUpdater(getCurrentUser());
                entityCode.setLastUpdate(now);
                codeRepository.save(entityCode);
            } else if (foundLink != null && foundIntroductionPage) {
                // existing link, introduction page exists, update link
                foundLink.setLink(condition.getIntroductionUrl());
                foundLink.setLastUpdater(getCurrentUser());
                foundLink.setLastUpdate(now);
                entityCode.setLastUpdate(now);
                codeRepository.save(entityCode);
            } else if (foundLink != null && !foundIntroductionPage){
                // existing link, introduction page does not exist, remove link
                entityCode.getLinks().remove(foundLink);
                entityCode.setLastUpdate(now);
                codeRepository.save(entityCode);
            }
        }
    }

    // to clear local:
    // DELETE FROM pv_link WHERE id > 5007022;
    // DELETE FROM pv_code WHERE standard_type_id = 134;
    @Override
    @Transactional
    public void synchroniseConditions() throws ResourceNotFoundException {
        // synchronise conditions previously retrieved from nhs choices, may be consolidated into once function call
        Lookup standardType = lookupRepository.findByTypeAndValue(
                LookupTypes.CODE_STANDARD, CodeStandardTypes.PATIENTVIEW.toString());
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

        // get codes and conditions to synchronise, finding all PATIENTVIEW Codes and all NhschoicesCondition
        List<Code> currentCodes = codeRepository.findAllByStandardType(standardType);
        List<NhschoicesCondition> conditions = nhschoicesConditionRepository.findAll();

        // map to store current Codes
        Map<String, Code> currentCodesMap = new HashMap<>();
        for (Code code : currentCodes) {
            currentCodesMap.put(code.getCode(), code);
        }

        Set<Code> codesToSave = new HashSet<>();
        List<NhschoicesCondition> conditionsToSave = new ArrayList<>();
        List<String> newOrUpdatedCodes = new ArrayList<>();

        LOG.info("Synchronising " + conditions.size() + " NhschoicesConditions with " + currentCodes.size()
                + " PATIENTVIEW standard type Codes");

        // iterate through all NHS Choices conditions
        for (NhschoicesCondition condition : conditions) {
            newOrUpdatedCodes.add(condition.getCode());

            // check if Code with same code as NhschoicesCondition exists in PV already
            if (currentCodesMap.keySet().contains(condition.getCode())) {
                // exists in patientview already, clear introduction url date and description date so updated next get
                condition.setIntroductionUrlLastUpdateDate(null);
                condition.setDescriptionLastUpdateDate(null);
                conditionsToSave.add(condition);


                Code currentCode = currentCodesMap.get(condition.getCode());
                boolean saveCurrentCode = false;

                // revert removed externally if set
                if (currentCode.isRemovedExternally()) {
                    currentCode.setRemovedExternally(false);
                    saveCurrentCode = true;
                }

                // if no patient friendly name then update with condition name
                if (StringUtils.isEmpty(currentCode.getPatientFriendlyName())) {
                    currentCode.setPatientFriendlyName(condition.getName());
                    saveCurrentCode = true;
                }

                // if changed, then save
                if (saveCurrentCode) {
                    codesToSave.add(currentCode);
                }
            } else {
                // NhschoicesCondition is new, create and save new Code
                Code code = new Code();
                code.setCreator(currentUser);
                code.setCreated(new Date());
                code.setLastUpdater(currentUser);
                code.setLastUpdate(new Date());
                code.setCode(condition.getCode());
                code.setCodeType(codeType);
                code.setSourceType(CodeSourceTypes.NHS_CHOICES);
                code.setStandardType(standardType);
                code.setDescription(condition.getName());
                code.setFullDescription(condition.getDescription());
                code.setPatientFriendlyName(condition.getName());
                code.setLastUpdate(code.getCreated());
                code.setLastUpdater(currentUser);

                // add Link to new Code if introduction URL is present on NhschoiceCondition
                if (StringUtils.isNotEmpty(condition.getIntroductionUrl())) {
                    org.patientview.persistence.model.Link nhschoicesLink
                            = new org.patientview.persistence.model.Link();
                    nhschoicesLink.setLink(condition.getIntroductionUrl());
                    nhschoicesLink.setName(NHS_CHOICES_LINK_DESCRIPTION);
                    nhschoicesLink.setCode(code);
                    nhschoicesLink.setCreator(currentUser);
                    nhschoicesLink.setCreated(code.getCreated());
                    nhschoicesLink.setLastUpdater(currentUser);
                    nhschoicesLink.setLastUpdate(code.getCreated());
                    nhschoicesLink.setDisplayOrder(1);
                    code.getLinks().add(nhschoicesLink);
                }

                codesToSave.add(code);
            }
        }

        // handle PATIENTVIEW codes that are no longer in NHS Choices Condition list, mark as removed externally
        for (Code code : currentCodes) {
            if (!newOrUpdatedCodes.contains(code.getCode())) {
                // Code has been removed externally
                code.setRemovedExternally(true);
                code.setLastUpdate(new Date());
                code.setLastUpdater(currentUser);
                codesToSave.add(code);
            }
        }

        if (!codesToSave.isEmpty()) {
            codeRepository.save(codesToSave);
        }

        if (!conditionsToSave.isEmpty()) {
            nhschoicesConditionRepository.save(conditionsToSave);
        }

        LOG.info("Finished synchronising " + conditions.size() + " NhschoicesConditions with " + currentCodes.size()
                + " PATIENTVIEW standard type Codes.");
    }

    @Override
    @Transactional
    public void updateConditions() throws ImportResourceException {
        String apiKey = properties.getProperty("nhschoices.api.key");
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
                        //if (!childNode.getTextContent().contains(".aspx")) {
                        uri = childNode.getTextContent().split("\\?apikey")[0];
                        //}
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
        List<NhschoicesCondition> currentConditions = nhschoicesConditionRepository.findAll();
        List<String> currentConditionCodes = new ArrayList<>();
        Map<String, NhschoicesCondition> currentUriMap = new HashMap<>();
        Map<String, NhschoicesCondition> currentCodeMap = new HashMap<>();

        for (NhschoicesCondition currentCondition : currentConditions) {
            currentUriMap.put(currentCondition.getUri(), currentCondition);
            currentConditionCodes.add(currentCondition.getCode());
            currentCodeMap.put(currentCondition.getCode(), currentCondition);
        }

        // set creator to importer if not called by a user from endpoint (e.g. run as task)
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            currentUser = userRepository.findByUsernameCaseInsensitive("importer");
        }

        List<String> newConditionCodes = new ArrayList<>();

        for (String uri : newUriMap.keySet()) {
            String conditionCode = getConditionCodeFromUri(uri);
            newConditionCodes.add(conditionCode);

            if (!currentUriMap.keySet().contains(uri)) {
                // new entry, introduction url and description are set on get Code to avoid limit
                NhschoicesCondition newCondition = newUriMap.get(uri);
                newCondition.setCreator(currentUser);
                newCondition.setCreated(new Date());
                newCondition.setCode(conditionCode);
                newCondition.setLastUpdate(newCondition.getCreated());
                newCondition.setLastUpdater(currentUser);
                nhschoicesConditionRepository.save(newCondition);
            } else {
                // existing entry, clear dates for introduction url and description so updated on next get Code
                NhschoicesCondition existingCondition = currentCodeMap.get(conditionCode);
                if (existingCondition != null) {
                    existingCondition.setIntroductionUrlLastUpdateDate(null);
                    existingCondition.setDescriptionLastUpdateDate(null);
                    existingCondition.setLastUpdate(new Date());
                    existingCondition.setLastUpdater(currentUser);
                    nhschoicesConditionRepository.save(existingCondition);
                }
            }
        }

        // delete old NhschoiceCondition, no longer on NHS Choices
        currentConditionCodes.removeAll(newConditionCodes);

        if (!currentConditionCodes.isEmpty()) {
            nhschoicesConditionRepository.deleteByCode(currentConditionCodes);
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
