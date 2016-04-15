package org.patientview.importer.service.impl;

import com.rabbitmq.client.Channel;
import generated.Patientview;
import generated.Survey;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.importer.service.QueueService;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Service
public class QueueServiceImpl extends AbstractServiceImpl<QueueServiceImpl> implements QueueService {

    private final static String QUEUE_NAME = "patient_import";
    private final static String QUEUE_NAME_SURVEY = "survey_import";

    @Inject
    @Named(value = "write")
    private Channel channel;

    public QueueServiceImpl() {

    }

    @PreDestroy
    public void tearDown() {
        try {
            channel.close();
        } catch (IOException io) {
            LOG.error("Error closing channel");
        }
    }

    @Override
    public void importRecord(final Patientview patientview) throws ImportResourceException {
        StringWriter stringWriter = new StringWriter();

        try {
            JAXBContext context = JAXBContext.newInstance(Patientview.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(patientview, stringWriter);
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall patientview record");
        }

        try {
            channel.basicPublish("", QUEUE_NAME, true, false, null, stringWriter.toString().getBytes());
            LOG.info("Successfully sent record to be processed for NHS number {}",
                    patientview.getPatient().getPersonaldetails().getNhsno());
        } catch (IOException e) {
            throw new ImportResourceException("Unable to send message onto queue");
        }

    }

    @Override
    public void importRecord(final Survey survey) throws ImportResourceException {
        StringWriter stringWriter = new StringWriter();

        try {
            JAXBContext context = JAXBContext.newInstance(Survey.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(survey, stringWriter);
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall survey record");
        }

        /*try {
            channel.basicPublish("", QUEUE_NAME_SURVEY, true, false, null, stringWriter.toString().getBytes());
            LOG.info("Added Survey description to queue");
        } catch (IOException e) {
            throw new ImportResourceException("Unable to send message onto queue");
        }*/
    }
}
