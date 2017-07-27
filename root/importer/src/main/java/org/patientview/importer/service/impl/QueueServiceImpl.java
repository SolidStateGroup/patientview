package org.patientview.importer.service.impl;

import com.rabbitmq.client.Channel;
import generated.Patientview;
import generated.Survey;
import generated.SurveyResponse;
import org.apache.commons.lang.StringUtils;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.service.QueueService;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.service.AuditService;
import org.patientview.service.SurveyResponseService;
import org.patientview.service.SurveyService;
import org.patientview.service.UkrdcService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.org.rixg.PatientRecord;

import javax.annotation.PostConstruct;
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
    private final static String QUEUE_NAME_SURVEY_RESPONSE = "survey_response_import";
    private final static String QUEUE_NAME_UKRDC = "ukrdc_import";

    @Inject
    private AuditService auditService;

    @Inject
    @Named(value = "write")
    private Channel channel;

    private Long importerUserId;

    @Inject
    private SurveyResponseService surveyResponseService;

    @Inject
    private SurveyService surveyService;

    @Inject
    private UkrdcService ukrdcService;

    public QueueServiceImpl() {

    }

    @PostConstruct
    public void init() throws ResourceNotFoundException {
        try {
            importerUserId = auditService.getImporterUserId();
        } catch (ResourceNotFoundException e) {
            LOG.error(e.getMessage());
            throw e;
        }
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
    public void importRecord(PatientRecord patientRecord) throws ImportResourceException {
        StringWriter stringWriter = new StringWriter();

        try {
            JAXBContext context = JAXBContext.newInstance(PatientRecord.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(patientRecord, stringWriter);
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall UKRDC PatientRecord");
        }

        // validate
        try {
            ukrdcService.validate(patientRecord);
        } catch (ImportResourceException ire) {
            LOG.info("UKRDC PatientRecord received, failed XML validation (" + ire.getMessage() + ")");

            if (!ire.isAnonymous()) {
                // attempt to get identifier if exists, used by audit
                String identifier = null;
                try {
                 identifier = ukrdcService.findIdentifier(patientRecord);
                } catch (ImportResourceException ire2) {
                    // no match in PV db, fall back to first patient number
                    if (patientRecord.getPatient() != null
                            && patientRecord.getPatient().getPatientNumbers() != null
                            && !CollectionUtils.isEmpty(
                            patientRecord.getPatient().getPatientNumbers().getPatientNumber())
                            && StringUtils.isNotEmpty(
                            patientRecord.getPatient().getPatientNumbers().getPatientNumber().get(0).getNumber())) {
                        identifier = patientRecord.getPatient().getPatientNumbers().getPatientNumber().get(0).getNumber();
                    }
                }

                // audit
                auditService.createAudit(AuditActions.UKRDC_VALIDATE_FAIL, identifier,
                        null, ire.getMessage(), stringWriter.toString(), importerUserId);
            }
            throw (ire);
        }

        // push to queue for processing
        try {
            channel.basicPublish("", QUEUE_NAME_UKRDC, true, false, null, stringWriter.toString().getBytes());
        } catch (IOException e) {
            throw new ImportResourceException("Unable to send UKRDC PatientRecord onto queue");
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

        // attempt to convert to objects
        try {
            JAXBContext context = JAXBContext.newInstance(Survey.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(survey, stringWriter);
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall survey");
        }

        // validate
        try {
            surveyService.validate(survey);
            LOG.info("Survey type '" + survey.getType() + "' Received, valid XML");
        } catch (ImportResourceException ire) {
            LOG.info("Survey type '" + survey.getType() + "' Received, failed XML validation (" + ire.getMessage()
                    + ")");
            // audit
            auditService.createAudit(AuditActions.SURVEY_VALIDATE_FAIL, null,
                    null, ire.getMessage(), stringWriter.toString(), importerUserId);
            throw (ire);
        }

        // push to queue for processing
        try {
            channel.basicPublish("", QUEUE_NAME_SURVEY, true, false, null, stringWriter.toString().getBytes());
            LOG.info("Added Survey description to '" + QUEUE_NAME_SURVEY + "' queue");
        } catch (IOException e) {
            throw new ImportResourceException("Unable to send Survey description to '" + QUEUE_NAME_SURVEY + "' queue");
        }
    }

    @Override
    public void importRecord(final SurveyResponse surveyResponse) throws ImportResourceException {
        StringWriter stringWriter = new StringWriter();

        // attempt to convert to objects
        try {
            JAXBContext context = JAXBContext.newInstance(SurveyResponse.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(surveyResponse, stringWriter);
        } catch (JAXBException jxb) {
            throw new ImportResourceException("Unable to marshall survey response");
        }

        // validate
        try {
            surveyResponseService.validate(surveyResponse);
        } catch (ImportResourceException ire) {
            LOG.info("SurveyResponse type '" + surveyResponse.getSurveyType() + "' Received, failed XML validation ("
                    + ire.getMessage() + ")");
            // audit
            auditService.createAudit(AuditActions.SURVEY_RESPONSE_VALIDATE_FAIL, surveyResponse.getIdentifier(),
                    null, ire.getMessage(), stringWriter.toString(), importerUserId);
            throw (ire);
        }

        // push to queue for processing
        try {
            channel.basicPublish("", QUEUE_NAME_SURVEY_RESPONSE, true, false, null, stringWriter.toString().getBytes());
        } catch (IOException e) {
            throw new ImportResourceException("Unable to send message onto queue");
        }
    }
}
