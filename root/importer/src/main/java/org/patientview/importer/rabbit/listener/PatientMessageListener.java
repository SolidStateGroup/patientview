package org.patientview.importer.rabbit.listener;

import com.rabbitmq.client.Channel;
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
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * An implementation of the message listener to consume messages from the 'pateint_importer' Queue.
 *
 * Once we receive the message we process it.
 * If processing is successful, this time use basicAck to confirm the message was consumed.
 *
 * Channel.basicNack(long deliveryTag, boolean multiple, boolean requeue)
 * - deliveryTag: the index of the message
 * - multiple：whether it's a batch. true: All messages smaller than the deliveryTag will be rejected at one time。
 * - requeue：Whether the rejected is re-queued
 *
 *
 * Created by Pavlo Maksymchuk.
 */
@Service("patientMessageListener")
public class PatientMessageListener implements ChannelAwareMessageListener {
    private static final Logger LOG = LoggerFactory.getLogger(PatientMessageListener.class);

    @Inject
    private EmailService emailService;
    @Inject
    private ImportManager importManager;
    @Inject
    private AuditService auditService;

    private Long importerUserId;

    @PostConstruct
    public void init() throws ResourceNotFoundException {
        try {
            importerUserId = auditService.getImporterUserId();
        } catch (ResourceNotFoundException e) {
            LOG.error(e.getMessage());
            throw e;
        }
    }

    /**
     * @param message
     * @param channel
     * @throws Exception
     * @see org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener#onMessage(org.springframework.amqp.core.Message,
     * com.rabbitmq.client.Channel)
     */
    public void onMessage(final Message message, final Channel channel) throws Exception {
        final String body = new String(message.getBody(), "utf-8");

        try {
            /*
             task handles all known issue with xml and should not throw any exception
             if exception thrown most like we have issue with DB
             so we need to retry processing message again
             */
            new PatientTask().process(body);

            // confirm message successfully consumed
            // false only confirms that the current message is received,
            // true confirms all messages obtained by the consumer
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            LOG.error("Failed to consume the message, re queueing:" , e);
            // un handled exception, resend back to the queue
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        }
    }

    /**
     * Inner class for processing Patient xml, copy of QueueProcessor.PatientTask
     */
    private class PatientTask {
        Patientview patient = null;

        public PatientTask() {
        }

        public void process(String message) {
            long start = System.currentTimeMillis();

            LOG.info("STARTING PatientTask to process message...");
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
         *
         * @param message              String error message
         * @param identifier           String patient identifier usually nhs number
         * @param unitCode             String unit/group code
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
         *
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
}
