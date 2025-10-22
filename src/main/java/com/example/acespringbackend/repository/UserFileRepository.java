//package com.example.acespringbackend.repository;
//
//import com.example.acespringbackend.model.UserFile;
//import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
//import reactor.core.publisher.Flux; // Required for Flux return type
//import reactor.core.publisher.Mono;
//
///**
// * Reactive repository for UserFile entities.
// * Provides methods for interacting with the userFiles collection in MongoDB.
// */
//public interface UserFileRepository extends ReactiveMongoRepository<UserFile, String> {
//
//    /**
//     * Finds a UserFile by its Google Drive file ID.
//     * @param driveFileId The Google Drive file ID.
//     * @return A Mono emitting the found UserFile, or empty if not found.
//     */
//    Mono<UserFile> findByDriveFileId(String driveFileId);
//
//    /**
//     * Finds all UserFiles belonging to a specific user.
//     * This method is used by DriveService to pre-fetch user file records.
//     * @param userId The ID of the user.
//     * @return A Flux emitting all UserFiles for the given user ID.
//     */
//    Flux<UserFile> findByUserId(String userId);
//    Mono<Void> deleteByUserId(String userId); // NEW METHOD
//
//    /**
//     * Finds a UserFile by its user ID and Google Drive file ID.
//     * Useful for verifying ownership before deletion or other operations.
//     * @param userId The ID of the user.
//     * @param driveFileId The Google Drive file ID.
//     * @return A Mono emitting the found UserFile, or empty if not found.
//     */
//    Mono<UserFile> findByUserIdAndDriveFileId(String userId, String driveFileId);
//}
// In src/main/java/com/example/acespringbackend/repository/UserFileRepository.java
package com.example.acespringbackend.repository;

import com.example.acespringbackend.model.UserFile;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserFileRepository extends ReactiveMongoRepository<UserFile, String> {

    Mono<UserFile> findByDriveFileId(String driveFileId);

    Flux<UserFile> findByUserId(String userId);

    Mono<Void> deleteByUserId(String userId);

    Mono<UserFile> findByUserIdAndDriveFileId(String userId, String driveFileId);

    // <--- ADD THIS NEW METHOD
    /**
     * Finds all UserFiles belonging to a specific user by their email address.
     * Requires the 'email' field to be present in the UserFile document.
     * @param email The email address of the user.
     * @return A Flux emitting all UserFiles for the given email.
     */
    Flux<UserFile> findByEmail(String email);
}