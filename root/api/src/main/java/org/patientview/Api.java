package org.patientview;

import org.patientview.api.config.ApiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by james@solidstategroup.com
 * Created on 04/06/2014
 */
@Configuration
@EnableAutoConfiguration
@Import(ApiConfig.class)
public class Api extends SpringBootServletInitializer {

    private static Class<Api> applicationClass = Api.class;

    public static void main(String args[]) {
        SpringApplication.run(applicationClass, args);

    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }
}
