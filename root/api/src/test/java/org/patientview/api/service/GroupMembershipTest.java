package org.patientview.api.service;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * Group membership test
 * Uses the output from the UserService sendGroupMemberShipNotification method, to validate against the external XSD
 *
 */
public class GroupMembershipTest {

    @Test
    public void test_valid() throws IOException, URISyntaxException, SAXException {
        URL schemaFile = new URL("https://raw.githubusercontent.com/renalreg/ukrdc/master/Schema/UKRDC.xsd");
        Source xmlFile = new StreamSource(getFileFromString("group-membership-valid.xml"));
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);
    }

    @Test(expected = SAXParseException.class)
    public void test_invalid() throws IOException, URISyntaxException, SAXException {
        URL schemaFile = new URL("https://raw.githubusercontent.com/renalreg/ukrdc/master/Schema/UKRDC.xsd");
        Source xmlFile = new StreamSource(getFileFromString("group-membership-invalid.xml"));
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Schema schema = schemaFactory.newSchema(schemaFile);
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);
    }


    File getFileFromString(String fileLocation) throws IOException, URISyntaxException {
        URL xmlPath = Thread.currentThread().getContextClassLoader().getResource(fileLocation);
        return new File(xmlPath.toURI());
    }

}
