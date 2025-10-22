package com.example.acespringbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class SecurityContextPropagationConfig {

    // This static block ensures the strategy is set very early in the application lifecycle.
    // It's executed when the class is loaded by the JVM.
    static {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        System.out.println("Spring Security Strategy set to MODE_INHERITABLE_THREAD_LOCAL."); // For verification
    }
}