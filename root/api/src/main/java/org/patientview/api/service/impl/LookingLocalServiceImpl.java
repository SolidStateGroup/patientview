package org.patientview.api.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.patientview.api.model.FhirDocumentReference;
import org.patientview.api.model.FhirMedicationStatement;
import org.patientview.api.model.FhirObservation;
import org.patientview.api.model.Patient;
import org.patientview.api.model.UserToken;
import org.patientview.api.service.ApiPatientService;
import org.patientview.api.service.AuthenticationService;
import org.patientview.api.service.DocumentService;
import org.patientview.api.service.LetterService;
import org.patientview.api.service.LookingLocalProperties;
import org.patientview.api.service.LookingLocalService;
import org.patientview.api.service.ApiMedicationService;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.api.service.ApiObservationService;
import org.patientview.config.exception.FhirResourceException;
import org.patientview.config.exception.ResourceForbiddenException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.config.utils.CommonUtils;
import org.patientview.persistence.model.FhirContact;
import org.patientview.persistence.model.FhirEncounter;
import org.patientview.persistence.model.FhirIdentifier;
import org.patientview.persistence.model.FhirPractitioner;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.enums.EncounterTypes;
import org.patientview.persistence.model.enums.IdentifierTypes;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    private DocumentService documentService;

    @Inject
    private LetterService letterService;

    @Inject
    private ApiMedicationService apiMedicationService;

    @Inject
    private ObservationHeadingService observationHeadingService;

    @Inject
    private ApiObservationService apiObservationService;

    @Inject
    private ApiPatientService apiPatientService;

    @Inject
    private Properties properties;

    private static final int LINE_LENGTH = 65;
    private static final int LINES_PER_PAGE = 10;
    public static final int MAX_WORD_SIZE = 10;

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

    @Override
    public String getDrugsXml(String token, int page)
            throws TransformerException, IOException, ParserConfigurationException {
        UserToken userToken;

        try {
            UserToken basicUserToken = new UserToken();
            basicUserToken.setToken(token);
            userToken = authenticationService.getUserInformation(basicUserToken);
        } catch (ResourceForbiddenException | ResourceNotFoundException rfe) {
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
                + LookingLocalProperties.LOOKING_LOCAL_DRUGS);
        formElement.setAttribute("method", "post");
        pageElement.appendChild(formElement);

        List<FhirMedicationStatement> fhirMedicationStatements;

        try {
            fhirMedicationStatements = apiMedicationService.getByUserId(userToken.getUser().getId());
        } catch (FhirResourceException | ResourceNotFoundException e) {
            return getErrorXml("Cannot get medication");
        }

        if (!CollectionUtils.isEmpty(fhirMedicationStatements)) {
            List<List<FhirMedicationStatement>> parts = Lists.partition(fhirMedicationStatements, 5);

            List<FhirMedicationStatement> fhirMedicationStatementsPage = parts.get(page);

            for (FhirMedicationStatement fhirMedicationStatement : fhirMedicationStatementsPage) {
                Element pageStatic = doc.createElement("static");
                pageStatic.setAttribute("value", fhirMedicationStatement.getName()
                        + " (" + fhirMedicationStatement.getDose() + ") "
                        + CommonUtils.dateToSimpleString(fhirMedicationStatement.getStartDate()));
                formElement.appendChild(pageStatic);
            }

            if (parts.size() > 1 && page < parts.size() - 1) {
                // more button
                Element more = doc.createElement("submit");
                more.setAttribute("name", "right");
                more.setAttribute("title", "More");
                formElement.appendChild(more);
            }
        } else {
            Element errorMessage = doc.createElement("static");
            errorMessage.setAttribute("value", "There is currently no medication data available.");
            formElement.appendChild(errorMessage);
        }

        // back button
        Element back = doc.createElement("submit");
        back.setAttribute("name", "left");
        back.setAttribute("title", "Back");
        formElement.appendChild(back);

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_DRUGS);
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
        fieldOption3.setAttribute("name", "Medication");
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
            UserToken basicUserToken = new UserToken();
            basicUserToken.setToken(token);
            userToken = authenticationService.getUserInformation(basicUserToken);
        } catch (ResourceForbiddenException | ResourceNotFoundException rfe) {
            return getErrorXml("Forbidden");
        }

        try {
            patientDetails = apiPatientService.getBasic(userToken.getUser().getId());
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
    public String getResultXml(String token, int page, String selection)
            throws TransformerException, IOException, ParserConfigurationException {
        UserToken userToken;

        try {
            UserToken basicUserToken = new UserToken();
            basicUserToken.setToken(token);
            userToken = authenticationService.getUserInformation(basicUserToken);
        } catch (ResourceForbiddenException | ResourceNotFoundException rfe) {
            return getErrorXml("Forbidden");
        }

        List<ObservationHeading> observationHeadings = observationHeadingService.findByCode(selection);

        if (CollectionUtils.isEmpty(observationHeadings)) {
            return getErrorXml("Error getting results");
        }

        Document doc = getDocument();

        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "My Results, " + observationHeadings.get(0).getHeading());
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_RESULT);
        formElement.setAttribute("method", "post");
        pageElement.appendChild(formElement);

        List<FhirObservation> observations;

        try {
            observations
                = apiObservationService.get(userToken.getUser().getId(), selection, "appliesDateTime", "DESC", null);
        } catch (FhirResourceException | ResourceNotFoundException | ResourceForbiddenException e) {
            return getErrorXml("Error getting results");
        }

        String units = observations.get(0).getUnits() != null ? observations.get(0).getUnits() : "";

        if (!CollectionUtils.isEmpty(observations)) {
            List<List<FhirObservation>> parts = Lists.partition(observations, 10);

            List<FhirObservation> observationsPage = parts.get(page);

            for (FhirObservation fhirObservation : observationsPage) {
                Element pageStatic = doc.createElement("static");
                pageStatic.setAttribute("value", fhirObservation.getValue() + "" + units + " ("
                        + CommonUtils.dateToSimpleString(fhirObservation.getApplies()) + ")");
                formElement.appendChild(pageStatic);
            }

            if (parts.size() > 1 && page < parts.size() - 1) {
                // more button
                Element more = doc.createElement("submit");
                more.setAttribute("name", "right");
                more.setAttribute("title", "More");
                formElement.appendChild(more);
            }
        } else {
            Element errorMessage = doc.createElement("static");
            errorMessage.setAttribute("value", "There are currently no results available.");
            formElement.appendChild(errorMessage);
        }

        // back button
        Element back = doc.createElement("submit");
        back.setAttribute("name", "left");
        back.setAttribute("title", "Back");
        formElement.appendChild(back);

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_RESULT);
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

        // selection
        Element selectionElement = doc.createElement("hiddenField");
        selectionElement.setAttribute("name", "selection");
        selectionElement.setAttribute("value", selection);
        formElement.appendChild(selectionElement);

        return outputXml(doc);
    }

    @Override
    public String getResultsXml(String token, int page)
            throws TransformerException, IOException, ParserConfigurationException {
        UserToken userToken;

        try {
            UserToken basicUserToken = new UserToken();
            basicUserToken.setToken(token);
            userToken = authenticationService.getUserInformation(basicUserToken);
        } catch (ResourceForbiddenException | ResourceNotFoundException rfe) {
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

        List<ObservationHeading> observationHeadings;

        try {
            observationHeadings
                    = observationHeadingService.getAvailableObservationHeadings(userToken.getUser().getId());
        } catch (FhirResourceException | ResourceNotFoundException e) {
            return getErrorXml("Error getting results");
        }

        if (!CollectionUtils.isEmpty(observationHeadings)) {
            // static element
            Element details = doc.createElement("static");
            details.setAttribute("value", "Select the type of result to view:");
            formElement.appendChild(details);

            // split into pages (max 10 rows per page)
            Collections.sort(observationHeadings, new Comparator<ObservationHeading>() {
                @Override
                public int compare(ObservationHeading o1, ObservationHeading o2) {
                    return o1.getHeading().compareTo(o2.getHeading());
                }
            });
            List<List<ObservationHeading>> parts = Lists.partition(observationHeadings, 9);

            List<ObservationHeading> observationHeadingsPage = parts.get(page);

            //  multisubmitField
            Element multisubmit = doc.createElement("multisubmitField");
            multisubmit.setAttribute("name", "selection");
            formElement.appendChild(multisubmit);

            for (ObservationHeading observationHeading : observationHeadingsPage) {
                Element fieldOption = doc.createElement("fieldOption");
                fieldOption.setAttribute("name", observationHeading.getHeading());
                fieldOption.setAttribute("value", observationHeading.getCode());
                multisubmit.appendChild(fieldOption);
            }

            if (parts.size() > 1 && page < parts.size() - 1) {
                // more button
                Element more = doc.createElement("submit");
                more.setAttribute("name", "right");
                more.setAttribute("title", "More");
                formElement.appendChild(more);
            }
        } else {
            Element errorMessage = doc.createElement("static");
            errorMessage.setAttribute("value", "There are currently no results available.");
            formElement.appendChild(errorMessage);
        }

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_RESULTS);
        formElement.appendChild(formAction);

        // back button
        Element back = doc.createElement("submit");
        back.setAttribute("name", "left");
        back.setAttribute("title", "Back");
        formElement.appendChild(back);

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
    public String getLetterXml(String token, int page, String selection)
            throws TransformerException, IOException, ParserConfigurationException {
        UserToken userToken;

        try {
            UserToken basicUserToken = new UserToken();
            basicUserToken.setToken(token);
            userToken = authenticationService.getUserInformation(basicUserToken);
        } catch (ResourceForbiddenException | ResourceNotFoundException rfe) {
            return getErrorXml("Forbidden");
        }

        Document doc = getDocument();
        Element formElement;


        List<FhirDocumentReference> fhirDocumentReferences;

        try {
            fhirDocumentReferences = documentService.getByUserIdAndClass(userToken.getUser().getId(), null, null, null);
        } catch (FhirResourceException | ResourceNotFoundException e) {
            return getErrorXml("Error getting letters");
        }

        // as no id given for documentreferences, must find by selection
        FhirDocumentReference found = null;

        if (!CollectionUtils.isEmpty(fhirDocumentReferences)) {
            for (FhirDocumentReference fhirDocumentReference : fhirDocumentReferences) {
                if (selection.equals(fhirDocumentReference.getDate().toString()
                    + fhirDocumentReference.getType())) {
                    found = fhirDocumentReference;
                }
            }
        }

        if (found != null) {
            boolean lastPage = false;

            // add page to screen
            Element pageElement = doc.createElement("page");
            pageElement.setAttribute("title", found.getType() + " ("
                    + CommonUtils.dateToSimpleString(found.getDate()) + ") ");
            pageElement.setAttribute("transform", "default");
            doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

            // add form to screen
            formElement = doc.createElement("form");
            formElement.setAttribute("action", properties.getProperty("api.url")
                    + LookingLocalProperties.LOOKING_LOCAL_LETTER);
            formElement.setAttribute("method", "post");
            pageElement.appendChild(formElement);

            // split letter into pages
            // display letter using static elements
            String letterContent = found.getContent();

            // create list of all lines
            String[] allLines = letterContent.split("\n");
            List<String> allLineList = Arrays.asList(allLines);
            List<String> finalLineList = new ArrayList<String>();

            // for each paragraph (as have split by \n) make sure each line only lineLength
            // long by adding new line after current line
            for (int i = 0; i < allLineList.size(); i++) {
                List<String> thisLine = new ArrayList<String>();
                thisLine.add(allLineList.get(i));

                for (int j = 0; j < thisLine.size(); j++) {
                    if (thisLine.get(j).length() > LINE_LENGTH) {

                        // handle splitting paragraph and not splitting words
                        int firstSpace;

                        if ((LINE_LENGTH - MAX_WORD_SIZE) > thisLine.get(j).length()) {
                            firstSpace = thisLine.get(j).length();
                        } else {
                            firstSpace = thisLine.get(j).indexOf(" ", LINE_LENGTH - MAX_WORD_SIZE);
                        }

                        if (firstSpace == -1) {
                            firstSpace = LINE_LENGTH;
                        }

                        // add new line from linelength to firstSpace
                        thisLine.add(j + 1, thisLine.get(j).substring(firstSpace, thisLine.get(j).length()).trim());

                        // clip this element
                        thisLine.set(j, thisLine.get(j).substring(0, firstSpace));
                        j--;
                    }
                }

                finalLineList.addAll(thisLine);
            }

            int totalItems = finalLineList.size();
            int start = page * LINES_PER_PAGE;
            int end = start + LINES_PER_PAGE;
            if (end > totalItems) {
                lastPage = true;
                end = totalItems;
            }

            // set paging at top
            Double totalPages = Math.floor((double) totalItems / (double) LINES_PER_PAGE) + 1;
            formElement.setAttribute("pagingText", "page " + (page + 1) + " of " + totalPages.intValue());

            List<String> letterSelection = finalLineList.subList(start, end);

            if (letterSelection.isEmpty()) {
                Element content = doc.createElement("static");
                content.setAttribute("value", "");
                formElement.appendChild(content);

                content = doc.createElement("static");
                content.setAttribute("value", "End of letter");
                formElement.appendChild(content);
            }

            for (String line : letterSelection) {
                Element content = doc.createElement("static");
                content.setAttribute("value", line);
                formElement.appendChild(content);
            }

            if (!lastPage) {
                // more button
                Element more = doc.createElement("submit");
                more.setAttribute("name", "right");
                more.setAttribute("title", "More");
                formElement.appendChild(more);
            }
        } else {
            // add page to screen
            Element pageElement = doc.createElement("page");
            pageElement.setAttribute("title", "Letter");
            pageElement.setAttribute("transform", "default");
            doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

            // add form to screen
            formElement = doc.createElement("form");
            formElement.setAttribute("action", properties.getProperty("api.url")
                    + LookingLocalProperties.LOOKING_LOCAL_LETTER);
            formElement.setAttribute("method", "post");
            pageElement.appendChild(formElement);

            Element errorMessage = doc.createElement("static");
            errorMessage.setAttribute("value", "Error retrieving letter.");
            formElement.appendChild(errorMessage);
        }

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_LETTER);
        formElement.appendChild(formAction);

        // back button
        Element back = doc.createElement("submit");
        back.setAttribute("name", "left");
        back.setAttribute("title", "Back");
        formElement.appendChild(back);

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

        // selection
        String foundSelection = "";

        if (found != null) {
            foundSelection = found.getDate().toString() + found.getType();
        }

        Element selectionElement = doc.createElement("hiddenField");
        selectionElement.setAttribute("name", "selection");
        selectionElement.setAttribute("value", foundSelection);
        formElement.appendChild(selectionElement);

        return outputXml(doc);
    }

    @Override
    public String getLettersXml(String token, int page)
            throws TransformerException, IOException, ParserConfigurationException {
        UserToken userToken;

        try {
            UserToken basicUserToken = new UserToken();
            basicUserToken.setToken(token);
            userToken = authenticationService.getUserInformation(basicUserToken);
        } catch (ResourceForbiddenException | ResourceNotFoundException rfe) {
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
                + LookingLocalProperties.LOOKING_LOCAL_LETTERS);
        formElement.setAttribute("method", "post");
        pageElement.appendChild(formElement);

        List<FhirDocumentReference> fhirDocumentReferences;

        try {
            fhirDocumentReferences = documentService.getByUserIdAndClass(userToken.getUser().getId(), null, null, null);
        } catch (FhirResourceException | ResourceNotFoundException e) {
            return getErrorXml("Error getting letters");
        }

        if (!CollectionUtils.isEmpty(fhirDocumentReferences)) {
            // static element
            Element details = doc.createElement("static");
            details.setAttribute("value", "Select the letter to view:");
            formElement.appendChild(details);

            // split into pages (max 10 rows per page)
            List<List<FhirDocumentReference>> parts = Lists.partition(fhirDocumentReferences, 9);

            List<FhirDocumentReference> fhirDocumentReferencesPage = parts.get(page);

            //  multisubmitField
            Element multisubmit = doc.createElement("multisubmitField");
            multisubmit.setAttribute("name", "selection");
            formElement.appendChild(multisubmit);

            for (FhirDocumentReference fhirDocumentReference : fhirDocumentReferencesPage) {
                Element fieldOption = doc.createElement("fieldOption");
                fieldOption.setAttribute("name", fhirDocumentReference.getType()
                        + " (" + CommonUtils.dateToSimpleString(fhirDocumentReference.getDate()) + ")");
                fieldOption.setAttribute("value",
                        fhirDocumentReference.getDate().toString() + fhirDocumentReference.getType());
                multisubmit.appendChild(fieldOption);
            }

            if (parts.size() > 1 && page < parts.size() - 1) {
                // more button
                Element more = doc.createElement("submit");
                more.setAttribute("name", "right");
                more.setAttribute("title", "More");
                formElement.appendChild(more);
            }
        } else {
            Element errorMessage = doc.createElement("static");
            errorMessage.setAttribute("value", "There are currently no letters available.");
            formElement.appendChild(errorMessage);
        }

        // form action
        Element formAction = doc.createElement("hiddenField");
        formAction.setAttribute("name", "formAction");
        formAction.setAttribute("value", properties.getProperty("api.url")
                + LookingLocalProperties.LOOKING_LOCAL_LETTERS);
        formElement.appendChild(formAction);

        // back button
        Element back = doc.createElement("submit");
        back.setAttribute("name", "left");
        back.setAttribute("title", "Back");
        formElement.appendChild(back);

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
}
