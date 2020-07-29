package org.patientview.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS Configuration for the application.
 */
@Configuration
public class CORSConfig implements WebMvcConfigurer {

    /**
     * Comma separated whitelisted URLs for CORS.
     * Should contain the applicationURL at the minimum.
     * Not providing this property would disable CORS configuration.
     */
    private static String[] allowedOrigins;

    /**
     * Methods to be allowed.
     */
    private static final String[] ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE"};

    /**
     * Request headers to be allowed.
     */
    private static final String[] ALLOWED_HEADERS = {
            "Accept",
            "Accept-Encoding",
            "Accept-Language",
            "Cache-Control",
            "Connection",
            "Content-Length",
            "Content-Type",
            "Content-Disposition",
            "Cookie",
            "Host",
            "Origin",
            "Pragma",
            "Referer",
            "User-Agent",
            "x-requested-with",
            "X-Auth-Toke",
            HttpHeaders.AUTHORIZATION};

    /**
     * CORS <code>maxAge</code> long property.
     */
    private static final long MAX_AGE = 3600L;


    public CORSConfig() {
    }

    /**
     * Configure CORS.
     *
     * @param registry a CorsRegistry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods(ALLOWED_METHODS);

//                .exposedHeaders(EXPOSED_HEADERS)
//                .allowCredentials(true)
//                .maxAge(MAX_AGE);
    }
}