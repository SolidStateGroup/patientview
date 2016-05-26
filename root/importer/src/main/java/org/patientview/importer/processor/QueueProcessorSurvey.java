package org.patientview.importer.processor;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import generated.Survey;
import org.patientview.config.exception.ImportResourceException;
import org.patientview.importer.manager.ImportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.ExecutorService;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 18/04/2016
 */
@Component
public class QueueProcessorSurvey extends DefaultConsumer {

    private final static Logger LOG = LoggerFactory.getLogger(QueueProcessorSurvey.class);

    @Inject
    private ExecutorService executor;

    private Channel channel;

    @Inject
    private ImportManager importManager;

    private final static String QUEUE_NAME = "survey_import";

    @Inject
    public QueueProcessorSurvey(@Named(value = "read") Channel channel) {
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
        LOG.info("Created queue processor for Surveys with queue name '" + QUEUE_NAME + "'");
    }

    @PreDestroy
    public void shutdown() throws IOException {
        channel.close();
    }

    private class SurveyTask implements Runnable {
        Survey survey = null;
        String message;

        public SurveyTask(String message) {
           this.message = message;
        }

        public void run() {
            boolean fail = false;

            try {
                JAXBContext jc = JAXBContext.newInstance(Survey.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                survey = (Survey) unmarshaller.unmarshal(new StringReader(message));
            } catch (JAXBException jbe) {
                LOG.error("Unable to unmarshall Survey record", jbe);
                fail = true;
            }

            // Process XML (already validated before being added to queue)
            if (!fail) {
                try {
                    importManager.process(survey);
                } catch (ImportResourceException ire) {
                    LOG.error("Survey type '" + survey.getType() + "' could not be added", ire);
                }
            }
        }
    }

    public void handleDelivery(String customerTag, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] body)
            throws IOException {
        executor.submit(new SurveyTask(new String(body)));
    }
}
