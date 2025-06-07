package com.example.acespringbackend.service;

public class DriveQuotaExceededException extends RuntimeException {
    public DriveQuotaExceededException(String message) {
        super(message);
    }
}