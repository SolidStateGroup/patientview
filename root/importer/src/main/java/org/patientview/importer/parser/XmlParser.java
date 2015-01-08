package org.patientview.importer.parser;

import generated.Patientview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

/**
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 */
public class XmlParser {

    private static final Logger LOG = LoggerFactory.getLogger(XmlParser.class);
    private static JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(Patientview.class);
        } catch (JAXBException jbe) {
            LOG.error("Unable to bind to generated classes");
        }
    }

    public static Patientview parse(File xmlFile) throws JAXBException {
        try {
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (Patientview) jaxbUnmarshaller.unmarshal(xmlFile);
        } catch (JAXBException jbe) {
            LOG.error("Unable to bind to generated class");
            throw jbe;
        }
    }
}
