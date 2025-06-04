// OTPStorageService.java
package com.example.acespringbackend.auth.dto;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPStorageService {

    private final ConcurrentHashMap<String, String> otpMap = new ConcurrentHashMap<>();

    public void storeOtp(String email, String otp) {
        otpMap.put(email, otp);
    }

    public boolean validateOtp(String email, String otp) {
        return otp.equals(otpMap.get(email));
    }

    public void removeOtp(String email) {
        otpMap.remove(email);
    }
}
