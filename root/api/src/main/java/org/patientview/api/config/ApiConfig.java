package org.patientview.api.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.patientview.api.aspect.SecurityAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Properties;


/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@Configuration
@ComponentScan(basePackages = {"org.patientview.api.service","org.patientview.api.aspect"})
@EnableWebMvc
public class ApiConfig {

    @Inject
    private Properties properties;

    @Bean
    @Primary
    public ObjectMapper getCustomerObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper(){
            @PostConstruct
            public void customConfiguration() {
                // Uses Enum.toString() for serialization of an Enum
                this.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
                // Uses Enum.toString() for deserialization of an Enum
                this.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

            }
        };

        return objectMapper;
    }


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

    @Bean
    public SecurityAspect securityAspect() {
        return new SecurityAspect();
    }
}




