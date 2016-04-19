package org.patientview.importer.processor;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import generated.Survey;
import generated.SurveyResponse;
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
 * Created on 19/04/2016
 */
@Component
public class QueueProcessorSurveyResponse extends DefaultConsumer {

    private final static Logger LOG = LoggerFactory.getLogger(QueueProcessorSurveyResponse.class);

    @Inject
    private ExecutorService executor;

    private Channel channel;

    @Inject
    private ImportManager importManager;

    private final static String QUEUE_NAME = "survey_response_import";

    @Inject
    public QueueProcessorSurveyResponse(@Named(value = "read") Channel channel) {
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
        LOG.info("Created queue processor for SurveyResponses with queue name '" + QUEUE_NAME + "'");
    }

    @PreDestroy
    public void shutdown() throws IOException {
        channel.close();
    }

    private class SurveyResponseTask implements Runnable {
        SurveyResponse surveyResponse = null;
        String message;

        public SurveyResponseTask(String message) {
           this.message = message;
        }

        public void run() {
            boolean fail = false;

            try {
                JAXBContext jc = JAXBContext.newInstance(SurveyResponse.class);
                Unmarshaller unmarshaller = jc.createUnmarshaller();
                surveyResponse = (SurveyResponse) unmarshaller.unmarshal(new StringReader(message));
            } catch (JAXBException jbe) {
                LOG.error("Unable to unmarshall SurveyResponse record", jbe);
                fail = true;
            }

            // validate XML
            if (!fail) {
                try {
                    importManager.validate(surveyResponse);
                    LOG.info("SurveyResponse type '" + surveyResponse.getSurveyType()
                            + "' Received, valid XML");
                } catch (ImportResourceException ire) {
                    LOG.info("SurveyResponse type '" + surveyResponse.getSurveyType()
                            + "' Received, failed XML validation");
                    fail = true;
                }
            }

            // Process XML
            if (!fail) {
                try {
                    importManager.process(surveyResponse);
                } catch (ImportResourceException ire) {
                    LOG.error("Survey type '" + surveyResponse.getSurveyType() + "' could not be added", ire);
                }
            }
        }
    }

    public void handleDelivery(String customerTag, Envelope envelope, AMQP.BasicProperties basicProperties, byte[] body)
            throws IOException {
        executor.submit(new SurveyResponseTask(new String(body)));
    }
}
