package com.example.acespringbackend; // Or wherever your main application class is

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository; // Make sure this is available
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories; // NEW IMPORT

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "com.example.acespringbackend.repository") // IMPORTANT: Specify your repository package
public class AceSpringBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AceSpringBackendApplication.class, args);
    }

}