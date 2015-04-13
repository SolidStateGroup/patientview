package org.patientview.importer.processor;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.Utility.Util;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.importer.service.AuditService;
import org.patientview.importer.service.EmailService;
import org.patientview.persistence.model.enums.AuditActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

/**
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 */
@Component
public class QueueProcessor extends DefaultConsumer {

    private final static Logger LOG = LoggerFactory.getLogger(QueueProcessor.class);

    private Long importerUserId;

    @Inject
    private ExecutorService executor;

    @Inject
    private ImportManager importManager;

    @Inject
    private AuditService auditService;

    @Inject
    private EmailService emailService;

    private Channel channel;

    @PostConstruct
    public void init() throws ResourceNotFoundException {
        try {
            importerUserId = auditService.getImporterUserId();
        } catch (ResourceNotFoundException e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    @Inject
    public QueueProcessor(@Named(value = "read") Channel channel) {
        super(channel);
        try {
            channel.basicConsume("patient_import", true, this);
        } catch (IOException io) {
            LOG.error("Cannot consume messages", io);
            throw new IllegalStateException("Cannot start queue processor");
        } catch (NullPointerException npe) {
            LOG.error("Queue is not available");
            throw new IllegalStateException("The queue is not available");
        }
        this.channel = channel;
        LOG.info("Create Request Processor");
    }

    @PreDestroy
    public void shutdown() throws IOException {
        channel.close();
    }

    private class PatientTask implements Runnable {
        Patientview patient = null;
        String message;

        public PatientTask(String message) {
           this.message = message;
        }

        public void run() {
            boolean fail = false;

            // Unmarshall XML to Patient object
            try {
                patient = Util.unmarshallPatientRecord(message);
            } catch (ImportResourceException ire) {
                LOG.error(ire.getMessage());
                auditService.createAudit(AuditActions.PATIENT_DATA_FAIL, null, null, 
                        ire.getMessage(), message, importerUserId);
                emailService.sendErrorEmail(ire.getMessage(), null, null);
                fail = true;
            }

            // if identifier not set
            if (!fail && patient.getPatient().getPersonaldetails().getNhsno() == null) {
                String errorMessage = "Identifier not set in XML";
                LOG.error(errorMessage);
                auditService.createAudit(AuditActions.PATIENT_DATA_VALIDATE_FAIL, null, null, 
                        errorMessage, message, importerUserId);
                emailService.sendErrorEmail(errorMessage, null, patient.getCentredetails().getCentrecode());
                fail = true;
            }

            // if group not set
            if ((!fail && patient.getCentredetails() == null)
                    || (!fail && patient.getCentredetails() != null 
                    && StringUtils.isEmpty(patient.getCentredetails().getCentrecode()))) {
                String errorMessage = "Group not set in XML";
                LOG.error(patient.getPatient().getPersonaldetails().getNhsno() + ": " + errorMessage);
                auditService.createAudit(AuditActions.PATIENT_DATA_VALIDATE_FAIL,
                        patient.getPatient().getPersonaldetails().getNhsno(), null, errorMessage, 
                        message, importerUserId);
                emailService.sendErrorEmail(errorMessage, patient.getPatient().getPersonaldetails().getNhsno(), null);
                fail = true;
            }

            // validate XML
            if (!fail) {
                try {
                    LOG.info(patient.getPatient().getPersonaldetails().getNhsno() + ": received");
                    importManager.validate(patient);
                } catch (ImportResourceException ire) {
                    String errorMessage = patient.getPatient().getPersonaldetails().getNhsno() 
                            + " ("
                            + patient.getCentredetails().getCentrecode()
                            + "): Failed validation, "
                            + ire.getMessage();
                    LOG.error(errorMessage);

                    auditService.createAudit(AuditActions.PATIENT_DATA_VALIDATE_FAIL,
                            patient.getPatient().getPersonaldetails().getNhsno(),
                            patient.getCentredetails().getCentrecode(), ire.getMessage(), message, importerUserId);
                    
                    emailService.sendErrorEmail(ire.getMessage(),
                            patient.getPatient().getPersonaldetails().getNhsno(),
                            patient.getCentredetails().getCentrecode());

                    fail = true;
                }
            }

            // Process XML
            if (!fail) {
                try {
                    importManager.process(patient, message, importerUserId);
                } catch (ImportResourceException ire) {
                    String errorMessage = patient.getPatient().getPersonaldetails().getNhsno()
                            + " ("
                            + patient.getCentredetails().getCentrecode()
                            + "): could not add. " 
                            + ire.getMessage();
                    LOG.error(errorMessage, ire);
                    auditService.createAudit(AuditActions.PATIENT_DATA_FAIL,
                            patient.getPatient().getPersonaldetails().getNhsno(),
                            patient.getCentredetails().getCentrecode(), ire.getMessage(), message, importerUserId);

                    // don't send emails for certain specific errors
                    if (errorMessageNeedsEmailSending(errorMessage)) {
                        emailService.sendErrorEmail(errorMessage, patient.getPatient().getPersonaldetails().getNhsno(),
                                patient.getCentredetails().getCentrecode());
                    }
                }
            }
        }

        private boolean errorMessageNeedsEmailSending(String errorMessage) {
            // don't send emails for FHIR stored procedure errors due to version id mismatch
            if (errorMessage.contains("ERROR: Wrong version_id")) {
                return false;
            }

            return true;
        }
    }

    public void handleDelivery(String customerTag, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] body)
            throws IOException {
        executor.submit(new PatientTask(new String(body)));
    }
}
