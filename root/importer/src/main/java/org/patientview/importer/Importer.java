package org.patientview.importer;

import org.patientview.config.CommonConfig;
import org.patientview.importer.config.ImporterConfig;
import org.patientview.persistence.config.PersistenceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by james@solidstategroup.com
 * Created on 21/08/2014
 */
@Configuration
@Import({ImporterConfig.class, PersistenceConfig.class, CommonConfig.class})
@EnableAutoConfiguration(exclude = { SecurityAutoConfiguration.class })
public class Importer extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(applicationClass, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }

    private static Class<Importer> applicationClass = Importer.class;
}
