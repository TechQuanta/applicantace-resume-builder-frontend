package com.example.acespringbackend.repository;

import com.example.acespringbackend.model.AtsResult;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository; // IMPORTANT: ReactiveMongoRepository
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono; // Import Mono from Project Reactor

@Repository
public interface AtsResultRepository extends ReactiveMongoRepository<AtsResult, String> {
    // Custom query method will now return Mono to align with reactive principles
    Mono<AtsResult> findByUserEmailAndFileName(String userEmail, String fileName);
}