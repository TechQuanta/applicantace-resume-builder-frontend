    package com.example.acespringbackend.config;

    import com.example.acespringbackend.service.JwtAuthenticationWebFilter;
    import com.example.acespringbackend.utility.JwtUtility;
    import com.example.acespringbackend.config.JwtAuthenticationWebEntryPoint;

    import org.springframework.beans.factory.annotation.Qualifier;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpMethod;
    import org.springframework.security.authentication.ReactiveAuthenticationManager;
    import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
    import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
    import org.springframework.security.config.web.server.ServerHttpSecurity;
    import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
    import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.server.SecurityWebFilterChain;
    import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
    import org.springframework.web.cors.reactive.CorsConfigurationSource;
    import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.reactive.CorsWebFilter;

    import java.util.Arrays;
    import java.util.Collections;

    @Configuration
    @EnableWebFluxSecurity
    public class SecurityConfig {

        private final JwtAuthenticationWebEntryPoint jwtAuthenticationWebEntryPoint;
        private final ReactiveUserDetailsService reactiveUserDetailsService; 

        public SecurityConfig(JwtAuthenticationWebEntryPoint jwtAuthenticationWebEntryPoint,
                              @Qualifier("customUserDetailsService") ReactiveUserDetailsService reactiveUserDetailsService) {
            this.jwtAuthenticationWebEntryPoint = jwtAuthenticationWebEntryPoint;
            this.reactiveUserDetailsService = reactiveUserDetailsService;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public ReactiveAuthenticationManager reactiveAuthenticationManager(PasswordEncoder passwordEncoder) {
            UserDetailsRepositoryReactiveAuthenticationManager manager =
                    new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService);
            manager.setPasswordEncoder(passwordEncoder);
            return manager;
        }

        @Bean
        public JwtAuthenticationWebFilter jwtAuthenticationWebFilter(JwtUtility jwtUtility) {
            return new JwtAuthenticationWebFilter(jwtUtility, reactiveUserDetailsService);
        }

        @Bean
        public CorsWebFilter corsWebFilter() {
            CorsConfiguration corsConfig = new CorsConfiguration();
            corsConfig.setAllowedOriginPatterns(Collections.singletonList("*"));
            corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
            corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
            corsConfig.setAllowCredentials(false);
            corsConfig.setMaxAge(3600L);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", corsConfig);
            return new CorsWebFilter(source);
        }

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                             JwtAuthenticationWebFilter jwtAuthenticationWebFilter,
                                                             CorsWebFilter corsWebFilter
                                                             ) {
            http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                
                .addFilterAt(corsWebFilter, SecurityWebFiltersOrder.CORS)

                .authorizeExchange(exchange -> exchange
                    .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll() 
                    .pathMatchers("/ace/auth/**").permitAll() 
                    // Revert ATS paths to require authentication
                    .pathMatchers(HttpMethod.POST, "/ats/checker/score").authenticated()
                    .pathMatchers(HttpMethod.POST, "/ats/checker/extract").authenticated()
                    // All other requests require authentication
                    .anyExchange().authenticated() 
                )
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(jwtAuthenticationWebEntryPoint)
                )
                .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION);

            return http.build();
        }
    }
    