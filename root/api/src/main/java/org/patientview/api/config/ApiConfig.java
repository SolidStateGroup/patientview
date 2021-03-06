package org.patientview.api.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.patientview.api.aspect.AuditAspect;
import org.patientview.api.aspect.SecurityAspect;
import org.patientview.api.client.FirebaseClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Base configuration for API
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@Configuration
@ComponentScan(basePackages = {"org.patientview.api.service", "org.patientview.api.aspect", "org.patientview.api.job",
        "org.patientview.persistence.resource", "org.patientview.service.*"})
@EnableWebMvc
@EnableScheduling
@EnableAsync
public class ApiConfig {

    @Inject
    private Properties properties;

    private static final int TEN_MB = 10485760;

    //TODO this just gets the "name" of the enum
    // remove and implement JSON shape object
    @Bean
    @Primary
    public ObjectMapper getCustomerObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper() {
            @PostConstruct
            public void customConfiguration() {
                // Uses Enum.toString() for serialization of an Enum
                this.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
                // RPV-768 trying to fix com.fasterxml.jackson.databind.JsonMappingException: failed to lazily
                // initialize a collection
                this.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
                // Uses Enum.toString() for deserialization of an Enum
                this.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
            }
        };

        return objectMapper;
    }

    @Bean
    public Jackson2ObjectMapperBuilder configureObjectMapper() {
        return new Jackson2ObjectMapperBuilder()
                .modulesToInstall(Hibernate5Module.class);
    }

    @Bean
    public Module datatypeHibernateModule() {
        return new Hibernate5Module();
    }

    @Bean
    public SecurityAspect securityAspectBean() {
         return SecurityAspect.aspectOf();
    }

    @Bean
    public AuditAspect auditAspectBean() {
        return AuditAspect.aspectOf();
    }

    /**
     * Configure JavaMailSender, used when sending emails.
     *
     * @return JavaMailSenderImpl with properties set from environment specific .properties file
     */
    @Bean
    public JavaMailSenderImpl javaMailSender() {
        final JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setHost(properties.getProperty("smtp.host"));
        javaMailSender.setUsername(properties.getProperty("smtp.username"));
        javaMailSender.setPassword(properties.getProperty("smtp.password"));

        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.debug", properties.getProperty("mail.debug"));
        javaMailProperties.setProperty("mail.smtp.auth", properties.getProperty("mail.smtp.auth"));
        javaMailProperties.setProperty("mail.smtp.ssl.enable", properties.getProperty("mail.smtp.ssl.enable"));
        javaMailProperties.setProperty("mail.smtp.starttls.enable",
                properties.getProperty("mail.smtp.starttls.enable"));
        javaMailProperties.setProperty("mail.smtp.port", properties.getProperty("mail.smtp.port"));
        javaMailSender.setJavaMailProperties(javaMailProperties);

        return javaMailSender;
    }

    /**
     * Configure ThreadPoolTaskExecutor used by migration process when migrating observations quickly from PatientView1
     *
     * @return ThreadPoolTaskExecutor
     */
    @Bean(name = "threadPoolTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(5);
        threadPoolTaskExecutor.setQueueCapacity(Integer.MAX_VALUE);
        return threadPoolTaskExecutor;
    }

    /**
     * Allow scheduled tasks to run on different threads.
     *
     * @return Executor
     */
    @Bean
    @Primary
    public Executor taskScheduler() {
        return Executors.newScheduledThreadPool(5);
    }

    /**
     * Configure CommonsMultipartResolver for use when receiving MultiPartFile uploads
     *
     * @return CommonsMultipartResolver
     */
    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setDefaultEncoding("utf-8");
        commonsMultipartResolver.setMaxUploadSize(TEN_MB); // 10MB
        return commonsMultipartResolver;
    }

    @Bean
    public FirebaseClient notificationClient() {
        return FirebaseClient.newBuilder()
                .setKey(properties.getProperty("firebase.api-key"))
                .setLive(new Boolean(properties.getProperty("firebase.live")))
                .build();
    }
}
