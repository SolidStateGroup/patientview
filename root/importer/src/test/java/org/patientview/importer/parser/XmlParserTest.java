package org.patientview.importer.parser;

import generated.Patientview;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 */
public class XmlParserTest {


    @Test
    public void testXmlLoading() throws IOException, URISyntaxException {

        URL xmlPath =
                Thread.currentThread().getContextClassLoader().getResource("data/xml/SAC02_01436_14251692001.xml");
        File file = new File(xmlPath.toURI());

        Assert.assertTrue("Test file not loaded", file.exists());

        Patientview patientRecord = XmlParser.parse(file);

        Assert.assertNotNull("A Patientview record should have been returned for the parser", patientRecord);

    }

}
