package org.patientview.api.config;

import org.patientview.api.filter.AuthenticateTokenFilter;
import org.patientview.api.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static java.util.Arrays.asList;

/**
 * Security configuration, Spring Security not used as security is managed through filters and authentication tokens.
 * Created by james@solidstategroup.com
 * Created on 16/06/2014
 */
@EnableWebSecurity
@ComponentScan(basePackages = {"org.patientview.api.controller", "org.patientview.api.filter"})
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(
            // all users login/logout/error/forgotten password
            new AntPathRequestMatcher("/auth/login"),
            new AntPathRequestMatcher("/auth/logout"),
            new AntPathRequestMatcher("/auth/forgottenpassword"),
            new AntPathRequestMatcher("/auth/userinformation"),
            new AntPathRequestMatcher("/error"),
            // importer user login
            new AntPathRequestMatcher("/import/login"),
            // mobile app login
            new AntPathRequestMatcher("/auth/loginmobile"),
            // public news
            new AntPathRequestMatcher("/public/news"),
            // public reviews
            new AntPathRequestMatcher("/public/reviews"),
            new AntPathRequestMatcher("/public/reviews/create"),
            // patient join requests
            new AntPathRequestMatcher("/public/group"),
            new AntPathRequestMatcher("/public/request"),
            // patient password request (contact unit)
            new AntPathRequestMatcher("/public/passwordrequest"),
            // verify email
            new AntPathRequestMatcher("/verify"),
            new AntPathRequestMatcher("/user/*/verify/*"),
            // used for ECS
            new AntPathRequestMatcher("/ecs/getpatientidentifiers"),
            // status for external info
            new AntPathRequestMatcher("/status"),
            // Swagger.io API documentation
            new AntPathRequestMatcher("/api-docs"),
            // Looking Local
            new AntPathRequestMatcher("/lookinglocal/home"),
            new AntPathRequestMatcher("/lookinglocal/auth"),
            // GP account creation
            new AntPathRequestMatcher("/gp/validatedetails"),
            new AntPathRequestMatcher("/gp/claim"),
            // external conversation creation
            new AntPathRequestMatcher("/conversations/external"),
            // external audit creation
            new AntPathRequestMatcher("/audit/external")
    );
    private static final RequestMatcher PROTECTED_URLS = new NegatedRequestMatcher(PUBLIC_URLS);

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable()
                .cors()
                .and()
                .addFilterBefore(authFilter(), AnonymousAuthenticationFilter.class)
                .authorizeRequests()
                .requestMatchers(PUBLIC_URLS).permitAll()
                .requestMatchers(PROTECTED_URLS).authenticated();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setAllowedMethods(asList("GET", "POST", "PUT", "DELETE"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return source;
    }

    @Bean
    AuthenticateTokenFilter authFilter() {

        return new AuthenticateTokenFilter();
    }

    @Override
    protected AuthenticationManager authenticationManager() {
        return authenticationService;
    }

    @Override
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
