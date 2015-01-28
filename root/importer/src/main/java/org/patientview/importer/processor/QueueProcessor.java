package org.patientview.importer.processor;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import generated.Patientview;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.importer.Utility.Util;
import org.patientview.importer.service.AuditService;
import org.patientview.persistence.model.Audit;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.User;
import org.patientview.persistence.model.enums.AuditActions;
import org.patientview.persistence.model.enums.AuditObjectTypes;
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
                createAudit(AuditActions.PATIENT_DATA_FAIL, null, null, ire.getMessage(), message);
                sendErrorEmail(ire.getMessage());
                fail = true;
            }

            // if identifier not set
            if (!fail && patient.getPatient().getPersonaldetails().getNhsno() == null) {
                String errorMessage = "Identifier not set in XML";
                LOG.error(errorMessage);
                createAudit(AuditActions.PATIENT_DATA_VALIDATE_FAIL, null, null, errorMessage, message);
                sendErrorEmail(errorMessage);
                fail = true;
            }

            // if group not set
            if (!fail && patient.getCentredetails() == null) {
                String errorMessage = "Group not set in XML";
                LOG.error(patient.getPatient().getPersonaldetails().getNhsno() + ": " + errorMessage);
                createAudit(AuditActions.PATIENT_DATA_VALIDATE_FAIL,
                        patient.getPatient().getPersonaldetails().getNhsno(), null, errorMessage, message);
                sendErrorEmail(errorMessage);
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
                            + "): failed validation, "
                            + ire.getMessage();
                    LOG.error(errorMessage);

                    createAudit(AuditActions.PATIENT_DATA_VALIDATE_FAIL,
                            patient.getPatient().getPersonaldetails().getNhsno(),
                            patient.getCentredetails().getCentrecode(), ire.getMessage(), message);
                    
                    sendErrorEmail(errorMessage);

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
                    createAudit(AuditActions.PATIENT_DATA_FAIL,
                            patient.getPatient().getPersonaldetails().getNhsno(),
                            patient.getCentredetails().getCentrecode(), ire.getMessage(), message);

                    sendErrorEmail(errorMessage);
                }
            }
        }
    }

    public void handleDelivery(String customerTag, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] body)
            throws IOException {
        executor.submit(new PatientTask(new String(body)));
    }

    private void createAudit(AuditActions auditActions, String identifier, String unitCode,
                             String information, String xml) {

        Audit audit = new Audit();
        audit.setAuditActions(auditActions);
        audit.setActorId(importerUserId);
        audit.setInformation(information);
        audit.setXml(xml);

        // attempt to set identifier and user being imported from identifier
        if (identifier != null) {
            audit.setIdentifier(identifier);
            User patientUser = auditService.getUserByIdentifier(identifier);
            if (patientUser != null) {
                audit.setSourceObjectId(patientUser.getId());
                audit.setSourceObjectType(AuditObjectTypes.User);
                audit.setUsername(patientUser.getUsername());
            }
        }

        // attempt to set group doing the importing
        if (unitCode != null) {
            Group group = auditService.getGroupByCode(unitCode);
            if (group != null) {
                audit.setGroup(group);
            }
        }

        auditService.save(audit);
    }
    
    private void sendErrorEmail(String errorMessage) {
        
    }
}
