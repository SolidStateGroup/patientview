package org.patientview.importer.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.io.IOException;

/**
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"org.patientview.importer.controller","org.patientview.importer.parser","org.patientview.importer.service"})
public class ImporterConfig {

    private final static Logger LOG = LoggerFactory.getLogger(ImporterConfig.class);

    private final static String QUEUE_NAME = "patient_import";

    @Bean
    public Channel channelWrite() {

        Channel channel = null;

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("ssg-user");
            factory.setPassword("ssg-user");
            factory.setVirtualHost("/ssg");
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            LOG.info("Successfully started messaging writer");
        } catch (IOException ioe) {
            LOG.error("Unable to connect to queue {}", ioe);
        }

        return channel;
    }


    @Bean
    public QueueingConsumer queueConsumerBean() {

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("ssg-user");
            factory.setPassword("ssg-user");
            factory.setVirtualHost("/ssg");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            QueueingConsumer queueingConsumer = new QueueingConsumer(channel);
            channel.basicConsume(QUEUE_NAME, false, queueingConsumer);
            LOG.info("Successfully started messaging reader");
            return queueingConsumer;
        } catch (IOException ioe) {
            LOG.error("Unable to connect to queue {}", ioe);
        }
        return null;
    }


}
