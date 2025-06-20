package com.swkim.safetrip.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile("!prod")
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("Content-Type", "Authorization")
                .allowCredentials(true)
                .exposedHeaders(accessHeader, refreshHeader)
                .maxAge(3600);
    }
}
