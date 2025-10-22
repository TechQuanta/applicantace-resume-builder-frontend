package com.example.acespringbackend.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for basic in-memory storage and retrieval of One-Time Passwords (OTPs).
 * This service uses a {@link ConcurrentHashMap} to store OTPs, associating each OTP
 * with a user's email address.
 *
 * <p>
 * **IMPORTANT CONSIDERATIONS FOR PRODUCTION:**
 * This simplified implementation *does not* include OTP expiration or single-use enforcement.
 * In a real-world production system, it is crucial for security and best practices to:
 * <ol>
 * <li><b>Add Time-Based Expiration:</b> OTPs should automatically expire after a short period (e.g., 5-10 minutes)
 * to prevent them from being valid indefinitely.</li>
 * <li><b>Enforce Single-Use:</b> OTPs should be invalidated (removed) immediately after successful validation
 * to prevent replay attacks.</li>
 * <li><b>Consider Persistent Storage:</b> For scalability, reliability, and to survive application restarts,
 * a more robust storage mechanism like Redis (with its Time-To-Live/TTL features) or a database
 * should be used instead of purely in-memory storage.</li>
 * <li><b>Handle Rate Limiting:</b> Implement measures to prevent brute-force attacks on OTP validation.</li>
 * </ol>
 * </p>
 */
@Service
public class OTPStorageService {

    /**
     * An in-memory {@link ConcurrentHashMap} used to store OTPs.
     * The key is the user's email address ({@link String}), and the value is the OTP ({@link String}).
     * {@link ConcurrentHashMap} is used to ensure thread-safe operations on the map
     * when accessed concurrently by multiple requests.
     */
    private final ConcurrentHashMap<String, String> otpMap = new ConcurrentHashMap<>();

    /**
     * Stores a given One-Time Password (OTP) and associates it with a specific email address.
     * If an OTP for the provided email already exists in the map, it will be overwritten
     * by the new OTP.
     *
     * @param email The email address of the user for whom the OTP is generated. Must not be null.
     * @param otp   The One-Time Password string to be stored. Must not be null.
     */
    public void storeOtp(String email, String otp) {
        otpMap.put(email, otp);
        // In a real application, you might add logging here for auditing purposes,
        // but avoid logging the OTP itself. E.g., log.debug("Stored OTP for email: {}", email);
    }

    /**
     * Validates a provided OTP against the OTP currently stored for a given email address.
     * This method retrieves the OTP associated with the email from the storage and
     * performs a direct string comparison.
     *
     * <p>
     * NOTE: This implementation does not remove the OTP after validation, nor does it
     * consider OTP expiration. For production, {@link #removeOtp(String)} should
     * typically be called after successful validation, and an expiration mechanism is crucial.
     * </p>
     *
     * @param email The email address of the user attempting to validate the OTP. Must not be null.
     * @param otp   The OTP string provided by the user for validation. Must not be null.
     * @return {@code true} if an OTP exists for the email and it matches the provided OTP;
     * {@code false} if no OTP is found for the email, or if the OTPs do not match.
     */
    public boolean validateOtp(String email, String otp) {
        // Retrieve the stored OTP; returns null if no entry for the email
        String storedOtp = otpMap.get(email);

        // Safely compare the provided OTP with the stored one.
        // If storedOtp is null, equals() will return false, avoiding NullPointerException.
        return otp.equals(storedOtp);
    }

    /**
     * Removes an OTP from storage for a specified email address.
     * This effectively invalidates any previously stored OTP for that email.
     *
     * <p>
     * This method is useful for cleaning up OTPs after successful validation or if
     * a user requests a new OTP before the previous one was used/expired.
     * </p>
     *
     * @param email The email address for which the OTP should be removed. Must not be null.
     */
    public void removeOtp(String email) {
        otpMap.remove(email);
        // In a real application, you might add logging here, e.g.,
        // log.debug("Removed OTP for email: {}", email);
    }
}