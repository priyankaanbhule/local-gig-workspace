package com.localgig.auth;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${localgig.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${localgig.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${localgig.mail.from:no-reply@localgig.local}")
    private String fromAddress;

    public PasswordResetEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    public void sendResetLink(String email, String token) {
        String baseUrl = frontendUrl.replaceAll("/+$", "");
        // A URL fragment is not sent to the server in HTTP requests or referrers.
        String resetLink = baseUrl + "/#reset?token=" + token;

        if (!mailEnabled) {
            System.getLogger(PasswordResetEmailService.class.getName()).log(
                    System.Logger.Level.INFO,
                    "Local password reset link for {0}: {1}", email, resetLink
            );
            return;
        }

        try {
            JavaMailSender mailSender = mailSenderProvider.getObject();
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("Reset your LocalGig password");
            message.setText("We received a request to reset your LocalGig password.\n\n"
                    + "Use this one-time link within 30 minutes:\n" + resetLink
                    + "\n\nIf you did not request this, you can ignore this email.");
            mailSender.send(message);
        } catch (Exception exception) {
            System.getLogger(PasswordResetEmailService.class.getName()).log(
                    System.Logger.Level.ERROR,
                    "Password reset email could not be delivered to {0}", email
            );
            // Keep the public response the same for existing and unknown emails.
        }
    }
}
