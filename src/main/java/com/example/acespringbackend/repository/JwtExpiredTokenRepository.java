// src/main/java/com/example/acespringbackend/repository/JwtExpiredTokenRepository.java
package com.example.acespringbackend.repository;

import com.example.acespringbackend.model.JwtExpiredToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface JwtExpiredTokenRepository extends ReactiveMongoRepository<JwtExpiredToken, String> {
    /**
     * Finds a JwtExpiredToken by its token string.
     * @param token The JWT string.
     * @return Mono<JwtExpiredToken> emitting the found token, or empty if not found.
     */
    Mono<JwtExpiredToken> findByToken(String token);

    /**
     * Finds a JwtExpiredToken by its token string and ensures it's not marked as used.
     * @param token The JWT string.
     * @param used Boolean indicating if the token should be unused (false).
     * @return Mono<JwtExpiredToken> emitting the found token, or empty if not found or already used.
     */
    Mono<JwtExpiredToken> findByTokenAndUsed(String token, boolean used);
}
