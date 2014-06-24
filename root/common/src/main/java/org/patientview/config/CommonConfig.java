package org.patientview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.io.IOException;
import java.util.Properties;

/**
 * TODO mask to load all environment property files instead of just API
 *
 * Created by james@solidstategroup.com
 * Created on 11/06/2014
 */
@Configuration
public class CommonConfig {

    private static String environment;

    @Value("${smtp.host}")
    private String host;
    @Value("${smtp.username}")
    private String username;
    @Value("${smtp.password}")
    private String password;

    static {
        environment = System.getProperty("env");
        if (environment == null) {
            throw new RuntimeException("Please specify and environment by using -Denv=local");
        }
    }

    @Bean
    public Properties properties() {
        try {
            Properties props = PropertiesLoaderUtils.loadAllProperties("conf/" + environment + "-api.properties");
            return props;
        } catch (IOException io) {
            throw new RuntimeException("Could not load property file");
        }
    }

    @Bean
    public JavaMailSenderImpl javaMailSender() {
        final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setHost(host);
        javaMailSender.setUsername(username);
        javaMailSender.setPassword(password);

        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.debug","false");
        javaMailProperties.setProperty("mail.smtp.auth","true");
        javaMailProperties.setProperty("mail.smtp.ssl.enable","true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable","true");
        javaMailProperties.setProperty("mail.smtp.port","465");
        javaMailSender.setJavaMailProperties(javaMailProperties);

        return javaMailSender;
    }
}
