// src/main/java/com/example/acespringbackend/service/EmailService.java
package com.example.acespringbackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("🔐 Your Signup Verification Code");
        helper.setText(
                "<html>" +
                        "<body>" +
                        "<h2 style='color: #2E86C1;'>Your OTP Code</h2>" +
                        "<p><strong>" + otp + "</strong></p>" +
                        "<p>This code is valid for 5 minutes. Do not share it with anyone.</p>" +
                        "<br/><hr/>" +
                        "<small style='color: gray;'>This is an automated email from AceSpringBackend.</small>" +
                        "</body>" +
                        "</html>",
                true // enable HTML
        );

        mailSender.send(message);
    }

    /**
     * Sends a password reset email with a unique link containing a JWT token.
     * @param to The recipient's email address.
     * @param resetLink The full password reset URL with the JWT token.
     * @throws MessagingException If there's an error sending the email.
     */
    public void sendPasswordResetEmail(String to, String resetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("Password Reset Request for Your AceSpringBackend Account");
        helper.setText(
                "<html>" +
                        "<body>" +
                        "<h2 style='color: #2E86C1;'>Password Reset Request</h2>" +
                        "<p>You have requested to reset your password. Please click the link below to proceed:</p>" +
                        "<p><a href=\"" + resetLink + "\" style='background-color: #2E86C1; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;'>Reset Password</a></p>" +
                        "<p>This link is valid for **20 minutes** only. If you did not request a password reset, please ignore this email.</p>" +
                        "<br/><hr/>" +
                        "<small style='color: gray;'>This is an automated email from AceSpringBackend.</small>" +
                        "</body>" +
                        "</html>",
                true // enable HTML
        );

        mailSender.send(message);
    }
}
