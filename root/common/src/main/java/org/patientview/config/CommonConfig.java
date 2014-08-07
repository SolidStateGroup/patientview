package org.patientview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

/**
 * TODO mask to load all environment property files instead of just API.
 *
 * Created by james@solidstategroup.com
 * Created on 11/06/2014
 */
@Configuration
public class CommonConfig {

    private static String environment;

    static {
        environment = System.getProperty("env");
        if (environment == null) {
            throw new RuntimeException("Please specify an environment by using -Denv=local");
        }
    }

    @Bean
    public Properties propertiesBean() {
        try {
            Properties props = PropertiesLoaderUtils.loadAllProperties("conf/" + environment + "-api.properties");
            return props;
        } catch (IOException io) {
            throw new RuntimeException("Could not load property file");
        }
    }

}
