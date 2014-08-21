package org.patientview.importer.util;

import generated.Patientview;
import org.patientview.importer.exception.ImportResourceException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
public class Util {


    public static StringWriter marshallPatientRecord(Patientview patientview) throws ImportResourceException {

        StringWriter stringWriter= null;

        try {
            JAXBContext context = JAXBContext.newInstance(Patientview.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(patientview, stringWriter);
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall patientview record");
        }

        return stringWriter;

    }

    public static Patientview umarshallPatientRecord(String content) throws ImportResourceException {
        try {
            JAXBContext jc = JAXBContext.newInstance(Patientview.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            return (Patientview) unmarshaller.unmarshal(new StringReader(content));
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall patientview record");
        }

    }


}
