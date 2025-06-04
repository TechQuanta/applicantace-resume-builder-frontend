package com.example.acespringbackend.service;

import com.example.acespringbackend.model.UserFile;
import com.example.acespringbackend.repository.UserFileRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 4 * 1024 * 1024; // 4 MB
    private static final int MAX_FILE_COUNT = 2;

    private final UserFileRepository userFileRepository;

    public FileStorageService(UserFileRepository userFileRepository) {
        this.userFileRepository = userFileRepository;
    }

    public Flux<UserFile> getUserFiles(String userId) {
        return userFileRepository.findByUserId(userId);
    }

    public Mono<UserFile> uploadFile(String userId, FilePart filePart) {
        return userFileRepository.findByUserId(userId)
                .collectList()
                .flatMap(existingFiles -> {
                    if (existingFiles.size() >= MAX_FILE_COUNT) {
                        return Mono.error(new RuntimeException("Max file upload limit reached"));
                    }

                    return filePart.headers().getContentLength() > MAX_FILE_SIZE
                            ? Mono.error(new RuntimeException("File too large"))
                            : saveFile(userId, filePart);
                });
    }

    private Mono<UserFile> saveFile(String userId, FilePart filePart) {
        String filename = filePart.filename();

        return filePart.content()
                .reduce(new byte[0], (prev, dataBuffer) -> {
                    byte[] next = new byte[prev.length + dataBuffer.readableByteCount()];
                    System.arraycopy(prev, 0, next, 0, prev.length);
                    dataBuffer.read(next, prev.length, dataBuffer.readableByteCount());
                    return next;
                })
                .flatMap(content -> {
                    UserFile userFile = UserFile.builder()
                            .userId(userId)
                            .filename(filename)
                            .size(content.length)
                            .contentType(filePart.headers().getContentType().toString())
                            .uploadedAt(LocalDateTime.now())
                            .build();
                    return userFileRepository.save(userFile);
                });
    }
}
