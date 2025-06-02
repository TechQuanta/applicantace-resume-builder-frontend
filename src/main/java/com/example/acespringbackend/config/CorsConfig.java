package com.example.acespringbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. SPECIFY YOUR FRONTEND ORIGIN EXACTLY
        // This is crucial if allowCredentials(true) is needed (which it often is for auth tokens)
        config.addAllowedOrigin("*"); // <-- Add your frontend's exact origin

        // If you might deploy to a specific domain, add it here too:
        // config.addAllowedOrigin("https://your-deployed-frontend.com");

        // Allowed methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allowed headers - Authorization header is often used for tokens
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With")); // Added "Accept" as good practice

        // 2. ALLOW CREDENTIALS - IMPORTANT FOR AUTHENTICATION TOKENS/COOKIES
        config.setAllowCredentials(true); // <-- Set to true for token/cookie-based authentication

        // Optional: Set max age for pre-flight requests to be cached by the browser
        config.setMaxAge(3600L); // Cache pre-flight response for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // Apply this CORS config to all paths

        return new CorsFilter(source);
    }
}