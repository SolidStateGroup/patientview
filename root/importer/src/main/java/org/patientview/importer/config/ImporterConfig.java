package org.patientview.importer.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by james@solidstategroup.com
 * Created on 14/07/2014
 */
@Configuration
@ComponentScan(basePackages = {"org.patientview.importer.*"})
@EnableWebMvc
public class ImporterConfig {

    private final static Logger LOG = LoggerFactory.getLogger(ImporterConfig.class);

    private final static String QUEUE_NAME = "patient_import";

    @Inject
    private Properties properties;

    @Bean(name = "write")
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


    @Bean(name = "read")
    public Channel channelRead() {

        Channel channel = null;

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("ssg-user");
            factory.setPassword("ssg-user");
            factory.setVirtualHost("/ssg");
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.basicQos(1);
            LOG.info("Successfully started messaging writer");
        } catch (IOException ioe) {
            LOG.error("Unable to connect to queue {}", ioe);
        }

        return channel;
    }

    @Bean
    public ExecutorService executorServiceBean() {
        return Executors.newFixedThreadPool(10);

    }

    /*
    @Bean(name = "fhir")
    public DataSource dataSourceBean() {

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("localhost:5432");
        dataSource.setUsername("fhir");
        dataSource.setPassword("fhir");
        return dataSource;
    }*/
}
