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
}
