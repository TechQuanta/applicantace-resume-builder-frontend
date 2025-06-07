package com.example.acespringbackend.repository;

import com.example.acespringbackend.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveMongoRepository<User, String> {

    Mono<User> findByEmail(String email);

    Mono<User> findByGithubId(String githubId);

}