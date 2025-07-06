////package com.example.acespringbackend.config;
////
////import com.example.acespringbackend.service.JwtAuthenticationFilter;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.http.HttpMethod;
////import org.springframework.security.authentication.AuthenticationManager;
////import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
////import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
////import org.springframework.security.config.http.SessionCreationPolicy;
////import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
////import org.springframework.security.crypto.password.PasswordEncoder;
////import org.springframework.security.web.SecurityFilterChain;
////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
////import org.springframework.security.web.authentication.HttpStatusEntryPoint;
////import org.springframework.web.cors.CorsConfiguration;
////import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
////import org.springframework.web.filter.CorsFilter;
////import org.springframework.http.HttpStatus;
////
////import java.util.List;
////
////@Configuration
////@EnableWebSecurity
////public class SecurityConfig {
////
////    private final JwtAuthenticationFilter jwtAuthenticationFilter;
////
////    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
////        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
////    }
////
////    @Bean
////    public CorsFilter corsFilter() {
////        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
////        CorsConfiguration config = new CorsConfiguration();
////        config.setAllowCredentials(true);
////        config.setAllowedOriginPatterns(List.of(
////            "http://localhost:5173",
////            "http://127.0.0.1:5173",
////            "https://your-frontend-domain.com" // Add your production domain here
////        ));
////        config.setAllowedHeaders(List.of("*"));
////        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
////        source.registerCorsConfiguration("/**", config);
////        return new CorsFilter(source);
////    }
////
////    @Bean
////    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
////        http
////            .csrf(AbstractHttpConfigurer::disable)
////            .authorizeHttpRequests(auth -> auth
////                // Allow all OPTIONS requests for CORS preflight checks
////                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
////                // Allow all requests to your authentication endpoints
////                .requestMatchers("/ace/auth/**").permitAll()
////                // Explicitly permit POST requests to the file upload endpoint (as per your original code)
////                .requestMatchers(HttpMethod.POST, "/ace/drive/**").permitAll()
////                // >>>>>> ADD THIS LINE FOR YOUR PROXYCURL ENDPOINT <<<<<<
////                .requestMatchers(HttpMethod.POST,"/ace/jot/**").permitAll()
////                // All other requests require authentication
////                .anyRequest().authenticated()
////            )
////            .formLogin(AbstractHttpConfigurer::disable)
////            .httpBasic(AbstractHttpConfigurer::disable)
////            .sessionManagement(session -> session
////                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
////            )
////            .exceptionHandling(exceptions -> exceptions
////                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
////            );
////
////        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
////
////        return http.build();
////    }
////
////    @Bean
////    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
////        return config.getAuthenticationManager();
////    }
////
////    @Bean
////    public PasswordEncoder passwordEncoder() {
////        return new BCryptPasswordEncoder();
////    }
////}
// src/main/java/com/example/acespringbackend/config/SecurityConfig.java
package com.example.acespringbackend.config;

import com.example.acespringbackend.service.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "https://your-frontend-domain.com" // Add your production domain here
        ));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Allow all OPTIONS requests for CORS preflight checks
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Allow all requests to your authentication endpoints
                .requestMatchers("/ace/auth/**").permitAll()
                // Explicitly permit POST requests to the file upload endpoint (as per your original code)
                .requestMatchers(HttpMethod.POST, "/ace/drive/**").permitAll()
                // Permit POST requests to the /ace/jot endpoint
                .requestMatchers(HttpMethod.POST,"/ace/jot/**").permitAll()
                // Permit POST requests to the /ats/checker/** endpoints
                .requestMatchers(HttpMethod.POST,"/ats/checker/**").permitAll()
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

//package com.example.acespringbackend.config;
//
//import com.example.acespringbackend.service.JwtAuthenticationFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.authentication.HttpStatusEntryPoint;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//import org.springframework.web.filter.CorsFilter;
//import org.springframework.http.HttpStatus;
//
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//    }
//
//    @Bean
//    public CorsFilter corsFilter() {
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        CorsConfiguration config = new CorsConfiguration();
//        config.setAllowCredentials(true);
//        config.setAllowedOriginPatterns(List.of(
//            "http://localhost:5173",
//            "[http://127.0.0.1:5173](http://127.0.0.1:5173)",
//            "[https://your-frontend-domain.com](https://your-frontend-domain.com)" // Add your production domain here
//        ));
//        config.setAllowedHeaders(List.of("*"));
//        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
//        source.registerCorsConfiguration("/**", config);
//        return new CorsFilter(source);
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//            .csrf(AbstractHttpConfigurer::disable)
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                .requestMatchers("/ace/auth/**").permitAll()
//                .requestMatchers(HttpMethod.POST, "/ace/drive/**").permitAll()
//                .requestMatchers(HttpMethod.POST,"/ace/jot").permitAll()
//                .requestMatchers(HttpMethod.POST,"/ats/checker/score").permitAll()// Allow POST to /ace/jot
//                .anyRequest().authenticated()
//            )
//            .formLogin(AbstractHttpConfigurer::disable)
//            .httpBasic(AbstractHttpConfigurer::disable)
//            .sessionManagement(session -> session
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            )
//            .exceptionHandling(exceptions -> exceptions
//                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
//            );
//
//        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
