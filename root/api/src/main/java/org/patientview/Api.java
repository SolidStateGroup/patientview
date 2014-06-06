package org.patientview;

import org.patientview.api.config.ApiConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Created by james@solidstategroup.com
 * Created on 04/06/2014
 */
@Configuration
@EnableAutoConfiguration
public class Api extends ApiConfig {

    public static void main(String args[]) {
        SpringApplication.run(new Object[]{Api.class}, args);

    }
}
