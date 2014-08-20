package org.patientview.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 24/06/2014
 */
@Configuration
@PropertySource("classpath:application.properties")
public class TestCommonConfig {

    private Properties properties;
    private Properties javaMailProperties;

    @Value("${smtp.host}")
    private String host;
    @Value("${smtp.username}")
    private String username;
    @Value("${smtp.password}")
    private String password;

    @PostConstruct
    public void init() {

        // todo: fails to load using @Value, works when set manually
        properties = new Properties();
        properties.setProperty("smtp.host", host);
        properties.setProperty("smtp.username", username);
        properties.setProperty("smtp.password", password);
        properties.setProperty("mail.debug","false");
        properties.setProperty("mail.smtp.auth","true");
        properties.setProperty("mail.smtp.ssl.enable","true");
        properties.setProperty("mail.smtp.starttls.enable","true");
        properties.setProperty("mail.smtp.port","465");
    }

    @Bean
    public JavaMailSenderImpl javaMailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(properties.getProperty("smtp.host"));
        javaMailSender.setUsername(properties.getProperty("smtp.username"));
        javaMailSender.setPassword(properties.getProperty("smtp.password"));
        javaMailSender.setJavaMailProperties(javaMailProperties);
        return javaMailSender;
    }

    @Bean
    public static PropertyPlaceholderConfigurer propertyPlaceholderConfigurer() {
        PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
        return ppc;
    }
}
