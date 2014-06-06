package org.patientview.api.config;

import org.patientview.persistence.config.PersistenceConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


/**
 * Created by james@solidstategroup.com
 * Created on 03/06/2014.
 */
@Configuration
@ComponentScan(basePackages = {"org.patientview.api.controller","org.patientview.api.service"})
@EnableWebMvc
public class ApiConfig extends PersistenceConfig {


}

