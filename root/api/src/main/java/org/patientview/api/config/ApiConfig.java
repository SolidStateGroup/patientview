package org.patientview.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.inject.Inject;
import java.util.Properties;


/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@Configuration
@ComponentScan(basePackages = {"org.patientview.api.service"})
@EnableWebMvc
public class ApiConfig {

    @Inject
    private Properties properties;

    @Bean
    public JavaMailSenderImpl javaMailSender() {
        final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setHost(properties.getProperty("smtp.host"));
        javaMailSender.setUsername(properties.getProperty("smtp.username"));
        javaMailSender.setPassword(properties.getProperty("smtp.password"));

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




