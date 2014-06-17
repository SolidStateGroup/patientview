package org.patientview.api.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
@EnableWebSecurity
@ComponentScan(basePackages = {"org.patientview.api.filter"})
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(WebSecurity web) throws Exception {

        // The security should not be used just yet
        web.ignoring().antMatchers("/**");

    }

}
