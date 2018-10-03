package org.patientview.importer.rabbit;

import org.patientview.importer.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Named;

/**
 * A simple service implementation to publish message to the RabbitMQ queue.
 *
 * Current implementation sends message to 'patient_import' queue.
 *
 * Created by Pavlo Maksymchuk.
 */
@Service
public class MessageProducer {

    private final static Logger LOG = LoggerFactory.getLogger(MessageProducer.class);

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(@Named(value = "patientTemplate") RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Sends message to the Queue.
     *
     * @param message a message to send to the queue
     * @throws Exception when failed to send message to the queue
     */
    public void sendMessage(Object message) throws Exception {
        LOG.info("Sending message to queue {}", RabbitConfig.PATIENT_QUEUE_NAME);

        this.rabbitTemplate.convertAndSend(RabbitConfig.PATIENT_QUEUE_NAME, message);
    }
}
