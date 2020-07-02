package org.patientview.importer.processor;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.importer.manager.ImportManager;
import org.patientview.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.org.rixg.PatientRecord;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 02/06/2016
 */
@Component
public class QueueProcessorUkrdc extends DefaultConsumer {

    @Inject
    private AuditService auditService;

    private Channel channel;

    @Inject
    private ExecutorService executor;

    @Inject
    private ImportManager importManager;

    private Long importerUserId;

    private final static Logger LOG = LoggerFactory.getLogger(QueueProcessorUkrdc.class);

    private final static String QUEUE_NAME = "ukrdc_import";

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
    public void shutdown() throws IOException, TimeoutException {
        channel.close();
    }

    @Inject
    public QueueProcessorUkrdc(@Named(value = "read") Channel channel) {
        super(channel);
        try {
            channel.basicConsume(QUEUE_NAME, true, this);
        } catch (IOException io) {
            LOG.error("Cannot consume messages for '" + QUEUE_NAME + "' queue", io);
            throw new IllegalStateException("Cannot start queue processor for '" + QUEUE_NAME + "' queue");
        } catch (NullPointerException npe) {
            LOG.error("Queue '" + QUEUE_NAME + "' is not available");
            throw new IllegalStateException("The '" + QUEUE_NAME + "' queue is not available");
        }
        this.channel = channel;
        LOG.info("Created queue processor for UKRDC PatientRecord with queue name '" + QUEUE_NAME + "'");
    }

    private class PatientRecordTask implements Runnable {
        PatientRecord patientRecord = null;
        String message;

        public PatientRecordTask(String message) {
           this.message = message;
        }

        public void run() {
            boolean fail = false;

            try {
                JAXBContext jc = JAXBContext.newInstance(PatientRecord.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                patientRecord = (PatientRecord) unmarshaller.unmarshal(new StringReader(message));
            } catch (JAXBException jbe) {
                LOG.error("Unable to unmarshall UKRDC PatientRecord record", jbe);
                fail = true;
            }

            // Process XML (already validated before being added to queue)
            if (!fail) {
                try {
                    importManager.process(patientRecord, message, importerUserId);
                } catch (ImportResourceException ire) {
                    LOG.error("UKRDC PatientRecord could not be added", ire);
                }
            }
        }
    }

    public void handleDelivery(String customerTag, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] body)
            throws IOException {
        executor.submit(new PatientRecordTask(new String(body)));
    }
}
