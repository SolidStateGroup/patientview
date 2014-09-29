package org.patientview.importer.processor;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import generated.Patientview;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.importer.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

/**
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 */
@Component
public class QueueProcessor extends DefaultConsumer {

    private final static Logger LOG = LoggerFactory.getLogger(QueueProcessor.class);

    @Inject
    private ExecutorService executor;

    @Inject
    private ImportManager importManager;

    @Inject
    private Properties properties;

    private Channel channel;

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

            try {
                patient = Util.unmarshallPatientRecord(message);
            } catch (ImportResourceException e) {
                LOG.error("Unable to recreate message");
            }

            LOG.info(patient.getPatient().getPersonaldetails().getNhsno() + " received");

            if (importManager.validate(patient)) {
                try {
                    importManager.process(patient);
                } catch (ImportResourceException rnf) {
                    LOG.error("Could not add patient NHS Number {}",
                            patient.getPatient().getPersonaldetails().getNhsno(), rnf);
                }

                if (Boolean.parseBoolean(properties.getProperty("remove.old.data"))) {
                    try {
                        importManager.removeOldData(patient);
                    } catch (ImportResourceException rnf) {
                        LOG.error("Could not remove old data for NHS Number {}",
                                patient.getPatient().getPersonaldetails().getNhsno(), rnf);
                    }
                }
            } else {
                LOG.error(patient.getPatient().getPersonaldetails().getNhsno() + " failed validation");
            }
        }
    }

    public void handleDelivery(String customerTag, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] body) throws IOException {
        Runnable task = new PatientTask(new String(body));
        executor.submit(task);
    }

}
