package org.patientview.api.config;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;
import java.util.Properties;

/**
 * Configuration of Swagger, used to display API documentation in web interface for external API developers.
 * Created by jamesr@solidstategroup.com
 * Created on 23/01/2015
 */
@Configuration
@EnableSwagger
@ComponentScan({ "org.patientview.api.controller" })
public class SwaggerConfig {

    private SpringSwaggerConfig springSwaggerConfig;

    @Inject
    private Properties properties;

    @Autowired
    public void setSpringSwaggerConfig(SpringSwaggerConfig springSwaggerConfig) {
        this.springSwaggerConfig = springSwaggerConfig;
    }

    @Bean
    public SwaggerSpringMvcPlugin customImplementation() {
        return new SwaggerSpringMvcPlugin(this.springSwaggerConfig)
                .apiInfo(apiInfo())
                .includePatterns(".*?")
                .excludeAnnotations(ExcludeFromApiDoc.class);
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "PatientView REST API",
                "The recommended REST API endpoints to be used when integrating with PatientView",
                properties.getProperty("site.url") + "/#/terms",
                null,
                null,
                null
        );
        return apiInfo;
    }
}
