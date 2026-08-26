package com.project.souklab.util;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.email.use-smtp:true}")
    private boolean useSmtp;

    @Value("${app.mailersend.api-key:}")
    private String mailerSendApiKey;

    @Value("${app.mailersend.sender-email:}")
    private String senderEmail;

    @Value("${app.mailersend.sender-name:Djezzy Talent}")
    private String senderName;

    @Async("applicationTaskExecutor")
    public void sendVerificationCode(String toEmail, String code) {
        String subject = "Account verification code";
        String htmlContent = "<p>Your verification code is: <strong>" + code + "</strong></p><p>This code expires soon.</p>";

        if (useSmtp) {
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
        String htmlContent = "<p>Your Djezzy Talent Portal verification code is: <strong>" + code + "</strong>.</p><p>It expires in 15 minutes.</p>";

        if (useSmtp) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("Password reset verification code");
                message.setText("Your Djezzy Talent Portal verification code is: " + code + ". It expires in 15 minutes.");
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

    private void sendViaMailerSend(String recipientEmail, String yourSubjectVariable, String yourHtmlContentVariable) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + mailerSendApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "from", Map.of("email", senderEmail, "name", senderName),
                "to", List.of(Map.of("email", recipientEmail)),
                "subject", yourSubjectVariable,
                "html", yourHtmlContentVariable
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        restTemplate.postForEntity("https://api.mailersend.com/v1/email", entity, String.class);
    }
}

