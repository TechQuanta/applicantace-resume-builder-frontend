package com.example.acespringbackend.repository;

import com.example.acespringbackend.model.UserFile;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface UserFileRepository extends ReactiveMongoRepository<UserFile, String> {
    Flux<UserFile> findByUserId(String userId);
}
