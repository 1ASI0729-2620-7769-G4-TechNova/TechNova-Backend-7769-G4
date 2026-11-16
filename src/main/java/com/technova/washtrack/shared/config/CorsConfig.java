package com.technova.washtrack.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Lets the Angular frontend (another origin) call the API. Origins come from washtrack.cors.allowed-origins. */
@Configuration
class CorsConfig {

    @Bean
    WebMvcConfigurer corsConfigurer(@Value("${washtrack.cors.allowed-origins:*}") String[] origins) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**").allowedOriginPatterns(origins).allowedMethods("*");
            }
        };
    }
}
