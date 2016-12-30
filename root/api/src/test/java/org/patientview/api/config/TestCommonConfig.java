package org.patientview.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
@Configuration
public class TestCommonConfig {

    @Inject
    private Properties properties;

    private static String environment;

    @Bean(name = "javaMailSender")
    public JavaMailSenderImpl javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(properties.getProperty("smtp.host"));

        // for testing a SMTP server with authentication, uncomment these lines
        // javaMailSender.setUsername(properties.getProperty("smtp.username"));
        // javaMailSender.setPassword(properties.getProperty("smtp.password"));

        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("smtp.host", properties.getProperty("smtp.host"));
        javaMailProperties.setProperty("smtp.username", properties.getProperty("smtp.username"));
        javaMailProperties.setProperty("smtp.password", properties.getProperty("smtp.password"));
        javaMailProperties.setProperty("mail.debug", properties.getProperty("mail.debug"));
        javaMailProperties.setProperty("mail.smtp.auth", properties.getProperty("mail.smtp.auth"));
        javaMailProperties.setProperty("mail.smtp.ssl.enable", properties.getProperty("mail.smtp.ssl.enable"));
        javaMailProperties.setProperty("mail.smtp.starttls.enable",
                properties.getProperty("mail.smtp.starttls.enable"));
        javaMailProperties.setProperty("mail.smtp.port", properties.getProperty("mail.smtp.port"));

        javaMailSender.setJavaMailProperties(javaMailProperties);
        return javaMailSender;
    }

    static {
        environment = System.getProperty("env");
        if (environment == null) {
            // set to local for local development purposes, should always be set as environment variable by Jenkins etc
            environment = "local";
        }
    }

    @Bean(name = "properties")
    public Properties propertiesBean() {
        try {
            Properties props = PropertiesLoaderUtils.loadAllProperties("conf/" + environment + "-api.properties");
            return props;
        } catch (IOException io) {
            throw new RuntimeException("Could not load property file 'conf/" + environment + "-api.properties'");
        }
    }
}
