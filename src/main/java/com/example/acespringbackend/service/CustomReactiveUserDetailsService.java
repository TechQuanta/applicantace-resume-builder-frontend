//package com.example.acespringbackend.service; // Adjust package
//
//import com.example.acespringbackend.model.User; // Assuming your User model
//import com.example.acespringbackend.repository.UserRepository; // Your ReactiveMongoRepository
//
//import io.jsonwebtoken.lang.Collections;
//
//import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//@Service
//public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {
//
//    private final UserRepository userRepository; // Inject your reactive user repository
//
//    public CustomReactiveUserDetailsService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public Mono<UserDetails> findByUsername(String username) {
//        // Assuming your UserRepository has a reactive findByEmail method
//        return userRepository.findByEmail(username) // Use your reactive repository to find the user
//                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
//                .map(user -> org.springframework.security.core.userdetails.User
//                        .withUsername(user.getEmail())
//                        .password(user.getPassword()) // Assuming password is encrypted
//                        .authorities(Collections.emptyList()) // Add actual roles/authorities if you have them
//                        .accountExpired(!((UserDetails) user).isAccountNonExpired())
//                        .accountLocked(!user.isAccountNonLocked())
//                        .credentialsExpired(!((UserDetails) user).isCredentialsNonExpired())
//                        .disabled(!((UserDetails) user).isEnabled())
//                        .build()
//                );
//    }
//}