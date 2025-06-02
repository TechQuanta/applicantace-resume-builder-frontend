package com.example.acespringbackend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthResponse {
    private String token;
    private String email;
    private String name;
    private String imageUrl;
    private String provider;
}
