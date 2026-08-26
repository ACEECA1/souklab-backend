package com.project.souklab.util;

import com.project.souklab.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailUtil.class);

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Async("applicationTaskExecutor")
    public void sendVerificationCode(String toEmail, String code) {
        String subject = "Account verification code";
        String htmlContent = "<p>Your verification code is: <strong>" + code + "</strong></p><p>This code expires soon.</p>";

        if (appProperties.getEmail().isUseSmtp()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("Account verification code");
                message.setText("Your verification code is: " + code + "\nThis code expires soon.");
                mailSender.send(message);
            } catch (Exception ex) {
                LOGGER.error("Failed to send verification code email via SMTP to {}", toEmail, ex);
            }
            return;
        }

        try {
            sendViaMailerSend(toEmail, subject, htmlContent);
        } catch (RestClientException ex) {
            LOGGER.error("Failed to send verification code email via MailerSend to {}", toEmail, ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while sending verification code email via MailerSend to {}", toEmail, ex);
        }
    }

    @Async("applicationTaskExecutor")
    public void sendPasswordResetCode(String toEmail, String code) {
        String subject = "Password reset verification code";
        String htmlContent = "<p>Your Souklab verification code is: <strong>" + code + "</strong>.</p><p>It expires in 15 minutes.</p>";

        if (appProperties.getEmail().isUseSmtp()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("Password reset verification code");
                message.setText("Your Souklab verification code is: " + code + ". It expires in 15 minutes.");
                mailSender.send(message);
            } catch (Exception ex) {
                LOGGER.error("Failed to send password reset email via SMTP to {}", toEmail, ex);
            }
            return;
        }

        try {
            sendViaMailerSend(toEmail, subject, htmlContent);
        } catch (RestClientException ex) {
            LOGGER.error("Failed to send password reset email via MailerSend to {}", toEmail, ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while sending password reset email via MailerSend to {}", toEmail, ex);
        }
    }

    @Async("applicationTaskExecutor")
    public void sendOAuthOnlyPasswordResetNotice(String toEmail) {
        String subject = "Password reset request for Souklab account";
        String htmlContent = "<p>Hello,</p>" +
                "<p>We received a password reset request for your Souklab account.</p>" +
                "<p>Your account is linked to Google Sign-In and does not use a separate password. " +
                "Please continue signing in directly with Google.</p>" +
                "<p>If you did not request this, you can safely ignore this email.</p>";

        if (appProperties.getEmail().isUseSmtp()) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject(subject);
                message.setText("Hello,\n\nWe received a password reset request for your Souklab account.\n\n" +
                        "Your account is linked to Google Sign-In and does not use a separate password. " +
                        "Please continue signing in directly with Google.\n\n" +
                        "If you did not request this, you can safely ignore this email.");
                mailSender.send(message);
            } catch (Exception ex) {
                LOGGER.error("Failed to send OAuth password reset notice email via SMTP to {}", toEmail, ex);
            }
            return;
        }

        try {
            sendViaMailerSend(toEmail, subject, htmlContent);
        } catch (RestClientException ex) {
            LOGGER.error("Failed to send OAuth password reset notice email via MailerSend to {}", toEmail, ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected error while sending OAuth password reset notice email via MailerSend to {}", toEmail, ex);
        }
    }

    private void sendViaMailerSend(String recipientEmail, String yourSubjectVariable, String yourHtmlContentVariable) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + appProperties.getMailersend().getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "from", Map.of("email", appProperties.getMailersend().getSenderEmail(), "name", appProperties.getMailersend().getSenderName()),
                "to", List.of(Map.of("email", recipientEmail)),
                "subject", yourSubjectVariable,
                "html", yourHtmlContentVariable
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity(appProperties.getMailersend().getApiUrl(), entity, String.class);
    }
}

