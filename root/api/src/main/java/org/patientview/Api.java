package org.patientview;

import org.patientview.api.config.ApiConfig;
import org.patientview.api.config.CacheConfig;
import org.patientview.api.config.SecurityConfig;
import org.patientview.api.config.SwaggerConfig;
import org.patientview.persistence.config.PersistenceConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

/**
 * Created by james@solidstategroup.com
 * Created on 04/06/2014
 *
 * https://spring.io/guides/tutorials/rest/5/
 */
@Configuration
@EnableAspectJAutoProxy
@EnableAutoConfiguration(exclude = AopAutoConfiguration.class)
@Import({ ApiConfig.class, PersistenceConfig.class, SecurityConfig.class, CacheConfig.class, SwaggerConfig.class })
public class Api extends SpringBootServletInitializer {

    private static Class<Api> applicationClass = Api.class;

    public static void main(String[] args) {
        SpringApplication.run(applicationClass, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }
}
