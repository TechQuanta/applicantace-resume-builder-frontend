package com.example.acespringbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // REMOVE allowedOriginPatterns("*") completely
                .allowedOrigins(
                        "https://applicantace.vercel.app",
                        "https://aceresume.techquanta.tech",
                        "http://localhost:5173",
                        "http://127.0.0.1:5173"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true) // Setting to 'true' is generally safer if you ever might use cookies/session.
                                        // For pure token-based auth (Bearer token in Authorization header, stored in localStorage), 'false' is fine.
                                        // But 'true' is more permissive and won't cause issues if you're not using cookies.
                .maxAge(3600);
    }

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        DefaultPartHttpMessageReader partReader = new DefaultPartHttpMessageReader();
        partReader.setMaxInMemorySize(10 * 1024 * 1024); // 10 MB
        MultipartHttpMessageReader multipartReader = new MultipartHttpMessageReader(partReader);
        configurer.defaultCodecs().multipartReader(multipartReader);
    }
}