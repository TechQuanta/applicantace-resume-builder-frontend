package com.example.acespringbackend.service;

import com.example.acespringbackend.utility.JwtUtility;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationWebFilter.class);

    private final JwtUtility jwtUtility;
    private final ReactiveUserDetailsService reactiveUserDetailsService;

    public JwtAuthenticationWebFilter(JwtUtility jwtUtility, ReactiveUserDetailsService reactiveUserDetailsService) {
        this.jwtUtility = jwtUtility;
        this.reactiveUserDetailsService = reactiveUserDetailsService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestPath = exchange.getRequest().getPath().value();
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // No need for debug logs here if info/error logs provide sufficient context
        // logger.debug("DEBUG JWT Filter: Processing request for path: {}", requestPath);
        // logger.debug("DEBUG JWT Filter: Authorization header: {}", (authHeader != null ? "Present" : "Absent"));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("JWT Filter: No Bearer token found or invalid format for path: {}", requestPath); // More specific debug
            return chain.filter(exchange);
        }

        String jwt = authHeader.substring(7);
        
        Mono<String> usernameMono = Mono.justOrEmpty(jwtUtility.extractUsername(jwt));

        return usernameMono
                .flatMap(username -> reactiveUserDetailsService.findByUsername(username))
                .flatMap(userDetails -> {
                    if (jwtUtility.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        logger.info("JWT WebFilter: Successfully authenticated user: {} for path: {}", userDetails.getUsername(), requestPath);
                        // No need for redundant debug log
                        // logger.debug("DEBUG JWT Filter: Authentication successful for path: {}", requestPath);

                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    } else {
                        logger.warn("JWT WebFilter: Token validation failed for user: {} for path: {}", userDetails.getUsername(), requestPath); // Changed to WARN
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }
                })
                .onErrorResume(e -> {
                    // This catches exceptions during username extraction, user loading, or other unexpected issues
                    logger.error("JWT WebFilter: Authentication error for path: {}. Details: {}", requestPath, e.getMessage()); // More concise error message
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // This case specifically handles when `usernameMono` is empty (username extraction failed)
                    // or `reactiveUserDetailsService.findByUsername` returns empty (user not found).
                    logger.warn("JWT WebFilter: Authentication failed for path: {}. Invalid token or user not found.", requestPath); // More descriptive WARN
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }));
    }
}