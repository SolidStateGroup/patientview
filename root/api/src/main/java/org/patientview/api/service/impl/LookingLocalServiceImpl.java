package org.patientview.api.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.patientview.api.model.Patient;
import org.patientview.api.model.UserToken;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.LookingLocalProperties;
import org.patientview.api.service.LookingLocalService;
import org.patientview.api.service.PatientService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirIdentifier;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

/**
 * Looking Local utility service.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 20/10/2015
 */
@Service
public class LookingLocalServiceImpl extends AbstractServiceImpl<LookingLocalServiceImpl>
        implements LookingLocalService {

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private PatientService patientService;

    @Inject
    private Properties properties;

    /**
     * Create XML for the authentication error screen in Looking Local
     */
    @Override
    public String getAuthErrorXml() throws TransformerException, IOException, ParserConfigurationException {
        Document doc = getDocument();
        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "There has been an authentication error");
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_HOME);
        formElement.setAttribute("method", "get");
        pageElement.appendChild(formElement);

        // static element
        Element details = doc.createElement("static");
        details.setAttribute("value", "We're sorry, the username/password combination was not recognised. "
                + "Please try again");
        formElement.appendChild(details);

        // home button
        Element home = doc.createElement("submit");
        home.setAttribute("name", "left");
        home.setAttribute("title", "Home");
        formElement.appendChild(home);

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url") + LookingLocalProperties.LOOKING_LOCAL_HOME);
        formElement.appendChild(formAction);

        // form method
        Element formMethod = doc.createElement("hiddenField");
        formMethod.setAttribute("name", "formMethod");
        formMethod.setAttribute("value", "get");
        formElement.appendChild(formMethod);

        return outputXml(doc);
    }

    /**
     * Utility function, creates empty XML document
     * @return Empty XML document
     */
    private static Document getDocument() throws ParserConfigurationException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        // root element: screen
        Document doc = docBuilder.newDocument();
        Element screenElement = doc.createElement("screen");
        screenElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        screenElement.setAttribute("xsi:noNamespaceSchemaLocation", "http://www.digitv.gov.uk/schemas/plugin.xsd");
        doc.appendChild(screenElement);

        return doc;
    }

    /**
     * Create XML for the general error screen in Looking Local
     */
    @Override
    public String getErrorXml(String errorText) throws TransformerException, IOException, ParserConfigurationException {
        Document doc = getDocument();
        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "There has been an error");
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_HOME);
        formElement.setAttribute("method", "get");
        pageElement.appendChild(formElement);

        // static element
        Element details = doc.createElement("static");
        details.setAttribute("value", "There has been an error. " + errorText);
        formElement.appendChild(details);

        // home button
        Element home = doc.createElement("submit");
        home.setAttribute("name", "left");
        home.setAttribute("title", "Home");
        formElement.appendChild(home);

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url") + LookingLocalProperties.LOOKING_LOCAL_HOME);
        formElement.appendChild(formAction);

        // form method
        Element formMethod = doc.createElement("hiddenField");
        formMethod.setAttribute("name", "formMethod");
        formMethod.setAttribute("value", "get");
        formElement.appendChild(formMethod);

        return outputXml(doc);
    }

    @Override
    public String getHomeXml() throws ParserConfigurationException, TransformerException, IOException {
        Document doc = getDocument();
        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "PatientView (PV) – view your results");
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_AUTH);
        formElement.setAttribute("method", "post");
        formElement.setAttribute("name", "blank");
        pageElement.appendChild(formElement);

        Element details = doc.createElement("static");
        details.setAttribute("value", "Please key in your details:");
        formElement.appendChild(details);

        Element username = doc.createElement("textField");
        username.setAttribute("hint", "Enter your username");
        username.setAttribute("label", "Username:");
        username.setAttribute("name", "username");
        username.setAttribute("size", "10");
        username.setAttribute("value", "");
        formElement.appendChild(username);

        Element password = doc.createElement("textField");
        password.setAttribute("hint", "Enter your Password");
        password.setAttribute("label", "Password:");
        password.setAttribute("name", "password");
        password.setAttribute("password", "true");
        password.setAttribute("size", "10");
        password.setAttribute("value", "");
        formElement.appendChild(password);

        // static element
        Element forget = doc.createElement("static");
        forget.setAttribute("value", "If you have forgotten your password, "
                + "please contact your unit administrator.");
        formElement.appendChild(forget);

        // sign-in button
        Element signIn = doc.createElement("submit");
        signIn.setAttribute("name", "left");
        signIn.setAttribute("title", "Sign in");
        formElement.appendChild(signIn);

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url") + LookingLocalProperties.LOOKING_LOCAL_AUTH);
        formElement.appendChild(formAction);

        // form method
        Element formMethod = doc.createElement("hiddenField");
        formMethod.setAttribute("name", "formMethod");
        formMethod.setAttribute("value", "post");
        formElement.appendChild(formMethod);

        //outputXml(doc, response);
        return outputXml(doc);
    }

    /**
     * Create XML for the Login Successful screen in Looking Local
     * @param token String token used to authenticate further requests
     */
    @Override
    public String getLoginSuccessfulXml(String token)
            throws TransformerException, IOException, ParserConfigurationException {
        Document doc = getDocument();
        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "Login Successful");
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_MAIN + "?token=" + token);
        formElement.setAttribute("method", "get");
        pageElement.appendChild(formElement);

        // static element
        Element details = doc.createElement("static");
        details.setAttribute("value", "You have successfully logged in.");
        formElement.appendChild(details);

        // home button
        Element home = doc.createElement("submit");
        home.setAttribute("name", "left");
        home.setAttribute("title", "Continue");
        formElement.appendChild(home);

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_MAIN + "?token=" + token);
        formElement.appendChild(formAction);

        // form method
        Element formMethod = doc.createElement("hiddenField");
        formMethod.setAttribute("name", "formMethod");
        formMethod.setAttribute("value", "post");
        formElement.appendChild(formMethod);


        // token
        Element tokenElement = doc.createElement("hiddenField");
        tokenElement.setAttribute("name", "token");
        tokenElement.setAttribute("value", token);
        formElement.appendChild(tokenElement);

        return outputXml(doc);
    }

    /**
     * Create XML for the main screen in Looking Local with core options
     */
    @Override
    public String getMainXml(String token) throws TransformerException, IOException, ParserConfigurationException {
        Document doc = getDocument();
        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "PatientView");
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_DETAILS + "?token=" + token);
        formElement.setAttribute("method", "post");
        pageElement.appendChild(formElement);

        // static element
        Element details = doc.createElement("static");
        details.setAttribute("value", "Select what you would like to look at:");
        formElement.appendChild(details);

        //  multisubmitField
        Element multisubmit = doc.createElement("multisubmitField");
        multisubmit.setAttribute("name", "selection");
        formElement.appendChild(multisubmit);

        // my details field Option
        Element fieldOption1 = doc.createElement("fieldOption");
        fieldOption1.setAttribute("name", "My Details");
        fieldOption1.setAttribute("value", "1");
        multisubmit.appendChild(fieldOption1);

        // medical result field Option
        Element fieldOption2 = doc.createElement("fieldOption");
        fieldOption2.setAttribute("name", "Medical Results");
        fieldOption2.setAttribute("value", "2");
        multisubmit.appendChild(fieldOption2);

        // drugs field Option
        Element fieldOption3 = doc.createElement("fieldOption");
        fieldOption3.setAttribute("name", "Drugs");
        fieldOption3.setAttribute("value", "3");
        multisubmit.appendChild(fieldOption3);

        // letters field Option
        Element fieldOption4 = doc.createElement("fieldOption");
        fieldOption4.setAttribute("name", "Letters");
        fieldOption4.setAttribute("value", "4");
        multisubmit.appendChild(fieldOption4);

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_DETAILS + "?token=" + token);
        formElement.appendChild(formAction);

        // form method
        Element formMethod = doc.createElement("hiddenField");
        formMethod.setAttribute("name", "formMethod");
        formMethod.setAttribute("value", "post");
        formElement.appendChild(formMethod);

        // token
        Element tokenElement = doc.createElement("hiddenField");
        tokenElement.setAttribute("name", "token");
        tokenElement.setAttribute("value", token);
        formElement.appendChild(tokenElement);

        return outputXml(doc);
    }

    @Override
    public String getMyDetailsXml(String token, int page)
            throws TransformerException, IOException, ParserConfigurationException {

        UserToken userToken;
        List<Patient> patientDetails;

        try {
            userToken = authenticationService.getUserInformation(token);
        } catch (ResourceForbiddenException rfe) {
            return getErrorXml("Forbidden");
        }

        try {
            patientDetails = patientService.getBasic(userToken.getUser().getId());
        } catch (FhirResourceException | ResourceNotFoundException e) {
            return getErrorXml("Error getting details");
        }

        Document doc = getDocument();

        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "My Details");
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_MY_DETAILS);
        formElement.setAttribute("method", "post");
        pageElement.appendChild(formElement);

        //Element pageStatic = doc.createElement("static");
        //pageStatic.setAttribute("value", String.valueOf(page));
        //formElement.appendChild(pageStatic);

        if (patientDetails.size() > 0) {
            Patient patient = patientDetails.get(page);

            Element groupName = doc.createElement("static");
            groupName.setAttribute("value", "Provided by: "
                    + (patient.getGroup() != null ? patient.getGroup().getName() : "unavailable"));
            formElement.appendChild(groupName);

            Element name = doc.createElement("static");
            name.setAttribute("value", "Name: "
                    + (patient.getFhirPatient().getForename() != null
                        ? patient.getFhirPatient().getForename() : "unavailable")
                    + (patient.getFhirPatient().getSurname() != null
                        ? " " + patient.getFhirPatient().getSurname() : ""));
            formElement.appendChild(name);

            Element dob = doc.createElement("static");
            dob.setAttribute("value", "Date of Birth: "
                    + (StringUtils.isNotEmpty(patient.getFhirPatient().getDateOfBirthNoTime())
                    ? patient.getFhirPatient().getDateOfBirthNoTime() : "unavailable"));
            formElement.appendChild(dob);

            if (!patient.getFhirPractitioners().isEmpty()) {
                FhirPractitioner gp = patient.getFhirPractitioners().get(0);

                Element gpName = doc.createElement("static");
                gpName.setAttribute("value", "GP Name: " + gp.getName());
                formElement.appendChild(gpName);

                Element gpAddress = doc.createElement("static");
                gpAddress.setAttribute("value", "GP Address: "
                        + (gp.getAddress1() != null ? gp.getAddress1()  + "," : "")
                        + (gp.getAddress2() != null ? " " + gp.getAddress2()  + "," : "")
                        + (gp.getAddress3() != null ? " " + gp.getAddress3()  + "," : "")
                        + (gp.getAddress4() != null ? " " + gp.getAddress4()  + "," : "")
                        + (gp.getPostcode() != null ? " " + gp.getPostcode() : ""));
                formElement.appendChild(gpAddress);

                String gpTelephoneNo = null;

                if (!gp.getContacts().isEmpty()) {
                    for (FhirContact contact : gp.getContacts()) {
                        if (StringUtils.isNotEmpty(contact.getValue())) {
                            gpTelephoneNo = contact.getValue();
                        }
                    }
                }

                if (gpTelephoneNo != null) {
                    Element gpTelephone = doc.createElement("static");
                    gpTelephone.setAttribute("value", "GP Telephone: " + gpTelephoneNo);
                    formElement.appendChild(gpTelephone);
                }
            }

            if (!patient.getFhirPatient().getIdentifiers().isEmpty()) {
                String identifier = null;
                String hospitalNumber = null;

                for (FhirIdentifier i : patient.getFhirPatient().getIdentifiers()) {
                    if (i.getLabel().equals(IdentifierTypes.CHI_NUMBER.toString())
                            || i.getLabel().equals(IdentifierTypes.HSC_NUMBER.toString())
                            || i.getLabel().equals(IdentifierTypes.NHS_NUMBER.toString())
                            || i.getLabel().equals(IdentifierTypes.NON_UK_UNIQUE.toString())
                    ) {
                        identifier = i.getValue();
                    } else if (i.getLabel().equals(IdentifierTypes.HOSPITAL_NUMBER.toString())) {
                        hospitalNumber = i.getValue();
                    }
                }

                if (identifier != null) {
                    Element identifierElement = doc.createElement("static");
                    identifierElement.setAttribute("value", "NHS/CHI/HSC Number: " + identifier);
                    formElement.appendChild(identifierElement);
                }

                if (hospitalNumber != null) {
                    Element identifierElement = doc.createElement("static");
                    identifierElement.setAttribute("value", "Hospital Number: " + hospitalNumber);
                    formElement.appendChild(identifierElement);
                }
            }

            if (!patient.getDiagnosisCodes().isEmpty()) {
                Element gpName = doc.createElement("static");
                gpName.setAttribute("value", "Diagnosis: " + patient.getDiagnosisCodes().get(0).getDescription());
                formElement.appendChild(gpName);
            }

            if (!patient.getFhirEncounters().isEmpty()) {
                String transplantStatusKidney = null;
                String treatment = null;

                for (FhirEncounter e : patient.getFhirEncounters()) {
                    if (e.getEncounterType().equals(EncounterTypes.TRANSPLANT_STATUS_KIDNEY.toString())) {
                        transplantStatusKidney = e.getStatus();
                    }
                    if (e.getEncounterType().equals(EncounterTypes.TREATMENT.toString())) {
                        treatment = e.getStatus();
                    }
                }

                if (transplantStatusKidney != null) {
                    Element gpName = doc.createElement("static");
                    gpName.setAttribute("value", "Transplant Status (Kidney): " + transplantStatusKidney);
                    formElement.appendChild(gpName);
                }

                if (treatment != null) {
                    Element gpName = doc.createElement("static");
                    gpName.setAttribute("value", "Treatment: " + treatment);
                    formElement.appendChild(gpName);
                }
            }
        } else {
            // no patient details found for this user, put error message
            Element errorMessage = doc.createElement("static");
            errorMessage.setAttribute("value", "There are no patient details available.");
            formElement.appendChild(errorMessage);
        }

        // back button
        Element back = doc.createElement("submit");
        back.setAttribute("name", "left");
        back.setAttribute("title", "Back");
        formElement.appendChild(back);

        if (patientDetails.size() > 1 && page < patientDetails.size() - 1) {
            // more button
            Element more = doc.createElement("submit");
            more.setAttribute("name", "right");
            more.setAttribute("title", "More");
            formElement.appendChild(more);
        }

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_MY_DETAILS);
        formElement.appendChild(formAction);

        // form method
        Element formMethod = doc.createElement("hiddenField");
        formMethod.setAttribute("name", "formMethod");
        formMethod.setAttribute("value", "post");
        formElement.appendChild(formMethod);

        // token
        Element tokenElement = doc.createElement("hiddenField");
        tokenElement.setAttribute("name", "token");
        tokenElement.setAttribute("value", token);
        formElement.appendChild(tokenElement);

        // page
        Element pageNumberElement = doc.createElement("hiddenField");
        pageNumberElement.setAttribute("name", "page");
        pageNumberElement.setAttribute("value", String.valueOf(page));
        formElement.appendChild(pageNumberElement);

        return outputXml(doc);
    }

    @Override
    public String getResultsXml(String token, int page)
            throws TransformerException, IOException, ParserConfigurationException {
        UserToken userToken;

        try {
            userToken = authenticationService.getUserInformation(token);
        } catch (ResourceForbiddenException rfe) {
            return getErrorXml("Forbidden");
        }

        Document doc = getDocument();

        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "My Results");
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_RESULTS);
        formElement.setAttribute("method", "post");
        pageElement.appendChild(formElement);

        ///
        Element pageStatic = doc.createElement("static");
        pageStatic.setAttribute("value", String.valueOf(page));
        formElement.appendChild(pageStatic);


        // back button
        Element back = doc.createElement("submit");
        back.setAttribute("name", "left");
        back.setAttribute("title", "Back");
        formElement.appendChild(back);

        if (page == 0) {
            // more button
            Element more = doc.createElement("submit");
            more.setAttribute("name", "right");
            more.setAttribute("title", "More");
            formElement.appendChild(more);
        }

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_RESULTS);
        formElement.appendChild(formAction);

        // form method
        Element formMethod = doc.createElement("hiddenField");
        formMethod.setAttribute("name", "formMethod");
        formMethod.setAttribute("value", "post");
        formElement.appendChild(formMethod);

        // token
        Element tokenElement = doc.createElement("hiddenField");
        tokenElement.setAttribute("name", "token");
        tokenElement.setAttribute("value", token);
        formElement.appendChild(tokenElement);

        // page
        Element pageNumberElement = doc.createElement("hiddenField");
        pageNumberElement.setAttribute("name", "page");
        pageNumberElement.setAttribute("value", String.valueOf(page));
        formElement.appendChild(pageNumberElement);

        return outputXml(doc);
    }

    @Override
    public String getDrugsXml(String token, int page)
            throws TransformerException, IOException, ParserConfigurationException {
        UserToken userToken;

        try {
            userToken = authenticationService.getUserInformation(token);
        } catch (ResourceForbiddenException rfe) {
            return getErrorXml("Forbidden");
        }

        Document doc = getDocument();

        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "My Medicines");
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_RESULTS);
        formElement.setAttribute("method", "post");
        pageElement.appendChild(formElement);

        ///
        Element pageStatic = doc.createElement("static");
        pageStatic.setAttribute("value", String.valueOf(page));
        formElement.appendChild(pageStatic);


        // back button
        Element back = doc.createElement("submit");
        back.setAttribute("name", "left");
        back.setAttribute("title", "Back");
        formElement.appendChild(back);

        if (page == 0) {
            // more button
            Element more = doc.createElement("submit");
            more.setAttribute("name", "right");
            more.setAttribute("title", "More");
            formElement.appendChild(more);
        }

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_RESULTS);
        formElement.appendChild(formAction);

        // form method
        Element formMethod = doc.createElement("hiddenField");
        formMethod.setAttribute("name", "formMethod");
        formMethod.setAttribute("value", "post");
        formElement.appendChild(formMethod);

        // token
        Element tokenElement = doc.createElement("hiddenField");
        tokenElement.setAttribute("name", "token");
        tokenElement.setAttribute("value", token);
        formElement.appendChild(tokenElement);

        // page
        Element pageNumberElement = doc.createElement("hiddenField");
        pageNumberElement.setAttribute("name", "page");
        pageNumberElement.setAttribute("value", String.valueOf(page));
        formElement.appendChild(pageNumberElement);

        return outputXml(doc);
    }

    @Override
    public String getLettersXml(String token, int page)
            throws TransformerException, IOException, ParserConfigurationException {
        UserToken userToken;

        try {
            userToken = authenticationService.getUserInformation(token);
        } catch (ResourceForbiddenException rfe) {
            return getErrorXml("Forbidden");
        }

        Document doc = getDocument();

        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "My Letters");
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_RESULTS);
        formElement.setAttribute("method", "post");
        pageElement.appendChild(formElement);

        ///
        Element pageStatic = doc.createElement("static");
        pageStatic.setAttribute("value", String.valueOf(page));
        formElement.appendChild(pageStatic);


        // back button
        Element back = doc.createElement("submit");
        back.setAttribute("name", "left");
        back.setAttribute("title", "Back");
        formElement.appendChild(back);

        if (page == 0) {
            // more button
            Element more = doc.createElement("submit");
            more.setAttribute("name", "right");
            more.setAttribute("title", "More");
            formElement.appendChild(more);
        }

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_RESULTS);
        formElement.appendChild(formAction);

        // form method
        Element formMethod = doc.createElement("hiddenField");
        formMethod.setAttribute("name", "formMethod");
        formMethod.setAttribute("value", "post");
        formElement.appendChild(formMethod);

        // token
        Element tokenElement = doc.createElement("hiddenField");
        tokenElement.setAttribute("name", "token");
        tokenElement.setAttribute("value", token);
        formElement.appendChild(tokenElement);

        // page
        Element pageNumberElement = doc.createElement("hiddenField");
        pageNumberElement.setAttribute("name", "page");
        pageNumberElement.setAttribute("value", String.valueOf(page));
        formElement.appendChild(pageNumberElement);

        return outputXml(doc);
    }

    /**
     * Write xml document to String and return
     * @param doc Input XML to output to String
     */
    private String outputXml(Document doc) throws TransformerException, IOException {

        // output string
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty("encoding", "ISO-8859-1");
        transformer.transform(domSource, result);

        return writer.toString();
    }

    /**
     * Write xml document to HTTP response
     * @param doc Input XML to output to HTTP response
     * @param response HTTP response
     */
    public static void outputXml(Document doc, HttpServletResponse response) throws TransformerException, IOException {

        // output string
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty("encoding", "ISO-8859-1");
        transformer.transform(domSource, result);

        String sb = writer.toString();

        response.setContentType("text/xml");
        response.setContentLength(sb.length());
        PrintWriter out;
        out = response.getWriter();
        out.println(sb);
        out.close();
        out.flush();
    }
}
