package com.example.acespringbackend.service;

import com.example.acespringbackend.model.User;
import com.example.acespringbackend.repository.UserRepository;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service // Spring will automatically name this bean "customUserDetailsService"
public class CustomUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String loginIdentifier) {
        return userRepository.findByEmail(loginIdentifier)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + loginIdentifier)))
                .map(user -> (UserDetails) user);
    }
}	