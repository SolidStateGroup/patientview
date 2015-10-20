package org.patientview.api.service.impl;

import org.patientview.api.service.LookingLocalService;
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
import java.util.Properties;

/**
 * Looking Local utility service.
 *
 * Created by jamesr@solidstategroup.com
 * Created on 20/10/2015
 */
@Service
public class LookingLocalServiceImpl extends AbstractServiceImpl<LookingLocalServiceImpl> implements LookingLocalService {

    public static final String LOOKING_LOCAL_HOME = "/lookinglocal/home";
    public static final String LOOKING_LOCAL_AUTH = "/lookinglocal/auth";
    public static final String LOOKING_LOCAL_ERROR = "/lookinglocal/error";
    public static final String LOOKING_LOCAL_ERROR_REDIRECT = "error";
    public static final String LOOKING_LOCAL_MAIN = "/lookinglocal/secure/main";
    public static final String LOOKING_LOCAL_MAIN_REDIRECT = "secure/main";
    public static final String LOOKING_LOCAL_DETAILS = "/lookinglocal/secure/details";
    public static final String LOOKING_LOCAL_MY_DETAILS = "/lookinglocal/secure/myDetails";
    public static final String LOOKING_LOCAL_DRUGS = "/lookinglocal/secure/drugs";
    public static final String LOOKING_LOCAL_LETTERS = "/lookinglocal/secure/letters";
    public static final String LOOKING_LOCAL_RESULTS_DISPLAY = "/lookinglocal/secure/resultsDisplay";
    public static final String LOOKING_LOCAL_RESULT_DISPLAY = "/lookinglocal/secure/resultDisplay";
    public static final String LOOKING_LOCAL_LETTER_DISPLAY = "/lookinglocal/secure/letterDisplay";

    @Inject
    Properties properties;

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
    public String getHomeXml() throws ParserConfigurationException, TransformerException, IOException {
        Document doc = getDocument();
        // add page to screen
        Element pageElement = doc.createElement("page");
        pageElement.setAttribute("title", "PatientView (PV) – view your results");
        pageElement.setAttribute("transform", "default");
        doc.getElementsByTagName("screen").item(0).appendChild(pageElement);

        // add form to screen
        Element formElement = doc.createElement("form");
        formElement.setAttribute("action", properties.getProperty("site.url") + LOOKING_LOCAL_AUTH);
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
        formAction.setAttribute("value", properties.getProperty("site.url") + LOOKING_LOCAL_AUTH);
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
