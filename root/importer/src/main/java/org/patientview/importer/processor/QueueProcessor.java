package org.patientview.importer.processor;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import generated.Patientview;
import org.apache.commons.lang.StringUtils;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.importer.service.EmailService;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.service.AuditService;
import org.patientview.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

/**
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 *
 * The current implementation has few issue with consuming the Queue ('patient_import').
 *
 * The processor has internal thread pool to process messages.
 * Once messages are pushed to RabbitMQ this processor will consume all the messages from the queue
 * and put them into JVM to be processed later if no thread available to use.
 *
 * This put strain on server resources and server restart or other issue will result in message loss.
 *
 * @deprecated this been deprecated in favour of Spring implementation
 */
@Deprecated
//@Component
public class QueueProcessor extends DefaultConsumer {

    @Inject
    private AuditService auditService;

    private Channel channel;

    @Inject
    private EmailService emailService;

    @Inject
    private ExecutorService executor;

    @Inject
    private ImportManager importManager;

    private Long importerUserId;

    private final static Logger LOG = LoggerFactory.getLogger(QueueProcessor.class);

    private final static String QUEUE_NAME = "patient_import";

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
            LOG.error("Cannot consume messages for '" + QUEUE_NAME + "' queue", io);
            throw new IllegalStateException("Cannot start queue processor for '" + QUEUE_NAME + "' queue");
        } catch (NullPointerException npe) {
            LOG.error("Queue '" + QUEUE_NAME + "' is not available");
            throw new IllegalStateException("The '" + QUEUE_NAME + "' queue is not available");
        }
        this.channel = channel;
        LOG.info("Created queue processor for Surveys with queue name '" + QUEUE_NAME + "'");
    }

    @PreDestroy
    public void shutdown() throws IOException, TimeoutException {
        channel.close();
    }

    private class PatientTask implements Runnable {
        Patientview patient = null;
        String message;

        public PatientTask(String message) {
           this.message = message;
        }

        public void run() {
            long start = System.currentTimeMillis();

            LOG.info("STARTING QueueProcessor PatientTask");
            boolean fail = false;

            // Unmarshall XML to Patient object
            try {
                patient = Util.unmarshallPatientRecord(message);
            } catch (ImportResourceException ire) {
                LOG.error(ire.getMessage());
                auditService.createAudit(AuditActions.PATIENT_DATA_FAIL, null, null,
                        ire.getMessage(), message, importerUserId);
                sendErrorEmail(ire.getMessage(), null, null, true);
                fail = true;
            }

            // if identifier not set
            if (!fail && patient.getPatient().getPersonaldetails().getNhsno() == null) {
                String errorMessage = "Identifier not set in XML";
                LOG.error(errorMessage);
                auditService.createAudit(AuditActions.PATIENT_DATA_VALIDATE_FAIL, null, null,
                        errorMessage, message, importerUserId);
                sendErrorEmail(errorMessage, null, patient.getCentredetails().getCentrecode(), true);
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
                sendErrorEmail(errorMessage, patient.getPatient().getPersonaldetails().getNhsno(), null, true);
                fail = true;
            }

            // validate XML
            if (!fail) {
                try {
                    LOG.info(patient.getPatient().getPersonaldetails().getNhsno() + ": Received, valid XML");
                    importManager.validate(patient);
                } catch (ImportResourceException ire) {
                    String errorMessage = patient.getPatient().getPersonaldetails().getNhsno()
                            + " (" + patient.getCentredetails().getCentrecode() + "): Failed validation, "
                            + ire.getMessage();
                    LOG.error(errorMessage);

                    auditService.createAudit(AuditActions.PATIENT_DATA_VALIDATE_FAIL,
                            patient.getPatient().getPersonaldetails().getNhsno(),
                            patient.getCentredetails().getCentrecode(), ire.getMessage(), message, importerUserId);

                    // RPV-697, only missing patient identifier will be sent to pv admins
                    boolean onlyToCentralSupport = !(errorMessage.contains("Patient")
                            && errorMessage.contains("does not exist"));

                    sendErrorEmail(errorMessage,
                            patient.getPatient().getPersonaldetails().getNhsno(),
                            patient.getCentredetails().getCentrecode(), onlyToCentralSupport);

                    fail = true;
                }
            }

            // Process XML
            if (!fail) {
                try {
                    importManager.process(patient, message, importerUserId);
                } catch (ImportResourceException ire) {
                    String errorMessage = patient.getPatient().getPersonaldetails().getNhsno()
                            + " (" + patient.getCentredetails().getCentrecode() + "): could not add. "
                            + ire.getMessage();
                    LOG.error(errorMessage, ire);
                    auditService.createAudit(AuditActions.PATIENT_DATA_FAIL,
                            patient.getPatient().getPersonaldetails().getNhsno(),
                            patient.getCentredetails().getCentrecode(), ire.getMessage(), message, importerUserId);

                    sendErrorEmail(errorMessage, patient.getPatient().getPersonaldetails().getNhsno(),
                            patient.getCentredetails().getCentrecode(), true);
                }
            }

            long stop = System.currentTimeMillis();
            LOG.info("TIMING PatientTask took: " + (stop - start) + " ms");
        }

        /**
         * Send importer error message to pv admins or central support using email service.
         * @param message String error message
         * @param identifier String patient identifier usually nhs number
         * @param unitCode String unit/group code
         * @param onlyToCentralSupport true if email should only be sent to central support
         */
        private void sendErrorEmail(String message, String identifier, String unitCode, boolean onlyToCentralSupport) {
            // don't send emails for certain specific errors
            if (errorMessageNeedsEmailSending(message)) {
                emailService.sendErrorEmail(message, identifier, unitCode, onlyToCentralSupport);
            }
        }

        /**
         * Ignore certain errors (do not need email sending either to admins or central support)
         * @param errorMessage String error message
         * @return true if email should be sent
         */
        private boolean errorMessageNeedsEmailSending(String errorMessage) {
            // don't send emails for FHIR stored procedure errors due to version id mismatch
            if (errorMessage.contains("ERROR: Wrong version_id")) {
                return false;
            }

            // don't send for out of disk space
            if (errorMessage.contains("No space left on device")) {
                return false;
            }

            return true;
        }
    }

    public void handleDelivery(String customerTag, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] body)
            throws IOException {
        LOG.info("QueueProcessor received message ....");
        executor.submit(new PatientTask(new String(body)));
    }
}
