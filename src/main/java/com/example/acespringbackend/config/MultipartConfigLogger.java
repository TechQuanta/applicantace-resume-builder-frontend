    // src/main/java/com/example/acespringbackend/config/MultipartConfigLogger.java
    package com.example.acespringbackend.config;

    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.stereotype.Component;

    @Component
    public class MultipartConfigLogger implements CommandLineRunner {

        @Value("${spring.servlet.multipart.max-file-size:1MB}") // Default to 1MB if not found
        private String maxFileSize;

        @Value("${spring.servlet.multipart.max-request-size:10MB}") // Default to 10MB if not found
        private String maxRequestSize;

        @Override
        public void run(String... args) throws Exception {
            System.out.println("-------------------------------------------------------");
            System.out.println("Multipart Configuration Check:");
            System.out.println("  spring.servlet.multipart.max-file-size: " + maxFileSize);
            System.out.println("  spring.servlet.multipart.max-request-size: " + maxRequestSize);
            System.out.println("-------------------------------------------------------");
        }
    }
    