package org.patientview.importer.config;

import org.patientview.importer.rabbit.listener.PatientMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import java.util.Properties;


/**
 * Created by Pavlo Maksymchuk.
 *
 * mq.rabbit.sessionCacheSize=100
 * mq.rabbit.maxConsumers=20
 * mq.concurrent.consumers=20
 * mq.prefetch.count=10
 */
@Configuration
@ComponentScan(basePackages = {"org.patientview.importer.*", "org.patientview.persistence.*", "org.patientview.service.*"})
public class RabbitConfig {

    private final static Logger LOG = LoggerFactory.getLogger(RabbitConfig.class);

    private final static int RABBIT_CONNECTION_SIZE = 100; // Set the number of connections 100
    private final static int RABBIT_PREFETCH_COUNT = 10; // maximum number of messages each consumer gets 10
    private final static int RABBIT_CONCURRENT_CONSUMERS = 20; // number of consumers 20

    public final static String PATIENT_QUEUE_NAME = "patient_import";

    @Inject
    private Properties properties;

    @Bean(name = "rabbitConnection")
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        //connectionFactory.setAddresses(rabbitmqIp);
        connectionFactory.setHost(properties.getProperty("rabbit.host"));
        connectionFactory.setUsername(properties.getProperty("rabbit.username"));
        connectionFactory.setPassword(properties.getProperty("rabbit.password"));
        // if not set will default to root virtual host /
        connectionFactory.setVirtualHost(properties.getProperty("rabbit.virtual.host"));
        // Set the number of connections
        connectionFactory.setChannelCacheSize(RABBIT_CONNECTION_SIZE);
        // enable if needed
        //connectionFactory.setPublisherConfirms(true);
        //connectionFactory.setPublisherReturns(true);
        return connectionFactory;
    }

    /**
     * We need RabbitTemplate to send messages to the RabbitMQ.
     * Consumer does not need it.
     *
     * Must be of type prototype (for different message callbacks)
     */
    @Bean("patientTemplate")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        // If you want to make setReturnCallback() and setConfirmCallback() take effect,
        // mandatory must be true, otherwise you can not get the returned message
        //template.setMandatory(true);
        //template.setReturnCallback(new ReturnCallbackListener());
        //template.setConfirmCallback(new ConfirmCallbackListener());
        // for custom message converter
        // template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean(name = "patientQueue")
    public Queue patientQueue() {
        return new Queue(PATIENT_QUEUE_NAME, true);
    }


    /**
     * Create and configure listener for the patient for patient_import Queue.
     *
     * Consumer receives message form the Queue, processes and responds with
     * acknowledgement, ensuring message is not discarded if there is unhandled error.
     *
     * @param patientMessageListener a listener for the 'patient_import' Queue
     */
    @Bean
    public SimpleMessageListenerContainer patientMessageContainer(PatientMessageListener patientMessageListener) {
        LOG.info("Initialized  consumer for queue: " + PATIENT_QUEUE_NAME);
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());
        container.setQueues(patientQueue());
        container.setExposeListenerChannel(true); // expose the channel to the listener to confirm manually
        container.setPrefetchCount(RABBIT_PREFETCH_COUNT);// maximum number of messages each consumer gets
        container.setConcurrentConsumers(RABBIT_CONCURRENT_CONSUMERS); // number of consumers
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); // set to manual confirmation
        container.setMessageListener(patientMessageListener);  // set message listener

        return container;
    }
}