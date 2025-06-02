package com.example.acespringbackend.auth.controller;

import com.example.acespringbackend.auth.dto.GoogleAuthRequest;
import com.example.acespringbackend.auth.dto.GoogleAuthResponse;
import com.example.acespringbackend.service.FirebaseAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/ace/auth")
public class FirebaseController {

    private final FirebaseAuthService firebaseAuthService;

    public FirebaseController(FirebaseAuthService firebaseAuthService) {
        this.firebaseAuthService = firebaseAuthService;
    }

    @PostMapping("/google")
    public Mono<ResponseEntity<GoogleAuthResponse>> loginWithGoogle(@RequestBody GoogleAuthRequest request) {
        return firebaseAuthService.authenticate(request)
                .map(ResponseEntity::ok)
                .onErrorResume(e ->
                        Mono.just(ResponseEntity.badRequest()
                                .body(new GoogleAuthResponse(null, null, null, null, "error: " + e.getMessage())))
                );
    }
}
