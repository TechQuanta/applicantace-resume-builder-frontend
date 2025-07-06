package com.example.acespringbackend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Year;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(JavaMailSender mailSender, @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("🔑 Your ApplicantAce Verification Code");
        helper.setText(
                "<!DOCTYPE html>" +
                        "<html lang='en'>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "<title>Your OTP Code - ApplicantAce</title>" +
                        "<style>" +
                        "body { font-family: 'Inter', Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }" +
                        ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); overflow: hidden; }" +
                        ".header { background-color: #2E86C1; padding: 30px; text-align: center; color: white; }" +
                        ".header h1 { margin: 0; font-size: 28px; }" +
                        ".content { padding: 30px; text-align: center; color: #333333; }" +
                        ".otp-box { background-color: #e8f5fd; color: #007bff; font-size: 36px; font-weight: bold; letter-spacing: 5px; padding: 15px 30px; border-radius: 8px; display: inline-block; margin: 20px 0; border: 1px solid #cce5ff; }" +
                        ".footer { background-color: #f0f0f0; padding: 20px; text-align: center; font-size: 12px; color: #777777; border-top: 1px solid #e0e0e0; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h1>🔑 Your ApplicantAce Verification Code</h1>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p style='font-size: 18px; line-height: 1.6;'>Hello,</p>" +
                        "<p style='font-size: 16px; line-height: 1.6;'>Thank you for signing up with ApplicantAce. Please use the following One-Time Password (OTP) to complete your verification:</p>" +
                        "<div class='otp-box'>" + otp + "</div>" +
                        "<p style='font-size: 16px; line-height: 1.6; color: #dc3545; font-weight: bold;'>This code is valid for 5 minutes.</p>" +
                        "<p style='font-size: 14px; line-height: 1.6; color: #666;'>For your security, please do not share this code with anyone.</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>&copy; " + java.time.Year.now().getValue() + " ApplicantAce. All rights reserved.</p>" +
                        "<p>This is an automated email. Please do not reply.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                true
        );

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String resetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("🔒 Password Reset Request for Your ApplicantAce Account");
        helper.setText(
                "<!DOCTYPE html>" +
                        "<html lang='en'>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "<title>Password Reset - ApplicantAce</title>" +
                        "<style>" +
                        "body { font-family: 'Inter', Arial, sans-serif; background-color: #f4f7f6; margin: 0; padding: 0; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; }" +
                        ".container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); overflow: hidden; }" +
                        ".header { background-color: #2E86C1; padding: 30px; text-align: center; color: white; }" +
                        ".header h1 { margin: 0; font-size: 28px; }" +
                        ".button { background-color: #2E86C1; color: white; padding: 12px 25px; text-decoration: none; border-radius: 6px; display: inline-block; font-size: 16px; font-weight: bold; margin-top: 20px; }" +
                        ".button:hover { background-color: #256a9e; }" +
                        ".footer { background-color: #f0f0f0; padding: 20px; text-align: center; font-size: 12px; color: #777777; border-top: 1px solid #e0e0e0; }" +
                        "</style>" +
                        "</head>" +
                        "<body>" +
                        "<div class='container'>" +
                        "<div class='header'>" +
                        "<h1>🔒 Password Reset Request</h1>" +
                        "</div>" +
                        "<div class='content'>" +
                        "<p style='font-size: 18px; line-height: 1.6;'>Hello,</p>" +
                        "<p style='font-size: 16px; line-height: 1.6;'>You have requested to reset your password for your ApplicantAce account. Please click the button below to proceed:</p>" +
                        "<p><a href=\"" + resetLink + "\" class='button'>Reset Password</a></p>" +
                        "<p style='font-size: 14px; line-height: 1.6; color: #dc3545; font-weight: bold;'>This link is valid for **20 minutes** only.</p>" +
                        "<p style='font-size: 14px; line-height: 1.6; color: #666;'>If you did not request a password reset, please ignore this email.</p>" +
                        "</div>" +
                        "<div class='footer'>" +
                        "<p>&copy; " + java.time.Year.now().getValue() + " ApplicantAce. All rights reserved.</p>" +
                        "<p>This is an automated email. Please do not reply.</p>" +
                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                true
        );

        mailSender.send(message);
    }

    /**
     * Sends an email notification about a file permission update.
     * @param to The recipient's email address (target user).
     * @param fileName The name of the file whose permissions were updated.
     * @param permissionRole The new permission role (e.g., "read", "write", "comment", or "removed").
     * @param actingUserEmail The email of the user who performed the action.
     * @param actionType The type of action (e.g., "granted", "updated", "removed").
     * @throws MessagingException If there's an error sending the email.
     */
    public void sendPermissionUpdateEmail(String to, String fileName, String permissionRole, String actingUserEmail, String actionType) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        log.debug("EmailService: Preparing to send permission update email.");
        log.debug("EmailService: To: {}, FileName: {}, PermissionRole: {}, ActingUserEmail: {}, ActionType: {}", to, fileName, permissionRole, actingUserEmail, actionType);

        helper.setFrom(fromEmail);
        helper.setTo(to);

        String subject;
        String iconEmoji;
        String actionVerb; // No headerColor needed for simplified template

        if ("removed".equalsIgnoreCase(actionType)) {
            subject = "❌ File Access Removed - ApplicantAce Notification";
            iconEmoji = "❌";
            actionVerb = "removed your access to";
        } else if ("updated".equalsIgnoreCase(actionType)) {
            subject = "🔗 File Access Updated - ApplicantAce Notification";
            iconEmoji = "🔗";
            actionVerb = "updated your access to";
        } else { // "granted"
            subject = "✅ New File Access Granted - ApplicantAce Notification";
            iconEmoji = "✅";
            actionVerb = "granted you access to";
        }

        log.debug("EmailService: Subject: {}", subject);
        helper.setSubject(subject);

        String headerText = iconEmoji + " " + subject.replace(" - ApplicantAce Notification", "");
        log.debug("EmailService: Calculated headerText: {}", headerText);

        // --- SIMPLIFIED HTML CONTENT FOR DEBUGGING ---
        String emailContent = String.format(
                "<!DOCTYPE html>" +
                        "<html lang='en'>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "<title>File Permission Update - ApplicantAce</title>" +
                        "</head>" +
                        "<body>" +
                        "<h1>%s</h1>" + // Just the combined header text
                        "<p>Hello,</p>" +
                        "<p><span >%s</span> has %s the file: <strong>\"%s\"</strong>.</p>" + // No user-email class or filename class
                        "<p>You now have <strong>%s</strong> access to this file.</p>" +
                        "<p>&copy; %s ApplicantAce. All rights reserved.</p>" + // Year
                        "</body>" +
                        "</html>",
                headerText,
                actingUserEmail,
                actionVerb,
                fileName,
                permissionRole,
                Year.now().getValue()
        );
        // --- END SIMPLIFIED HTML CONTENT ---


        log.debug("EmailService: Final HTML content (first 500 chars): {}", emailContent.substring(0, Math.min(emailContent.length(), 500)));
        log.debug("EmailService: Final HTML content (last 500 chars): {}", emailContent.substring(Math.max(0, emailContent.length() - 500)));


        try {
            helper.setText(emailContent, true);
            mailSender.send(message);
            log.info("EmailService: Successfully sent permission update email to {} for file {}", to, fileName);
        } catch (jakarta.mail.MessagingException e) {
            log.error("EmailService: Failed to send email to {} for file {}. Error: {}", to, fileName, e.getMessage(), e);
            throw e; // Re-throw the exception so DriveService can catch it.
        } catch (Exception e) {
            log.error("EmailService: An unexpected error occurred while sending email to {} for file {}. Error: {}", to, fileName, e.getMessage(), e);
            throw new MessagingException("Unexpected error during email send: " + e.getMessage(), e);
        }
    }
}