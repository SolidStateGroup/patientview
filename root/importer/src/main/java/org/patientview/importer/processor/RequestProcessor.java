package org.patientview.importer.processor;

import com.rabbitmq.client.QueueingConsumer;
import generated.Patientview;
import org.patientview.importer.util.Util;
import org.patientview.importer.exception.ImportResourceException;
import org.patientview.importer.service.PatientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;

/**
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 */
@Component
public class RequestProcessor implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(RequestProcessor.class);

    private boolean running;

    @Inject
    private PatientService patientService;

    @Inject
    QueueingConsumer queueingConsumer;

    @PostConstruct
    public void init() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @PreDestroy
    public void shutdown() throws IOException {
        // Try for 3 secs for a good shutdown
        running = false;
        try {
            Thread.currentThread().wait(3000L);
        } catch (Exception e) {
            LOG.error("Error pausing for shutdown");
        }
        // TODO check this is an interrupted exception for the process method
        // Hard shutdown
        queueingConsumer.getChannel().abort();

    }

    public void run() {
        running = true;
        try {
            while (running) {
                QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery();
                String message = new String(delivery.getBody());
                processPatient(message);
                queueingConsumer.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                LOG.info("Received request");
            }
        }
        catch (InterruptedException e) {
            LOG.info("Shutting down service");
        }
        catch (IOException ioe) {
            LOG.error("Shut down due to IO exception");
        }
    }

    public void processPatient(String message) {
        Patientview patient = null;

        try {
            patient = Util.umarshallPatientRecord(message);

        } catch (ImportResourceException e) {
            LOG.error("Unable to recreate message");
        }

        patientService.add(patient);

    }

}
