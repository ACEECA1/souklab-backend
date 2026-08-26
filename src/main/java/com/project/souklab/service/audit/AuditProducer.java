package com.project.souklab.service.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.project.souklab.config.RabbitMQConfig;
import com.project.souklab.dto.audit.AuditMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.project.souklab.model.AuditLogAction;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes an audit log message to the configured RabbitMQ exchange.
     * The message is routed using the audit routing key and will be processed asynchronously.
     *
     * @param action the {@link AuditLogAction} indicating the type of action performed
     * @param details a detailed description of the action and its context
     * @param username the username of the user who performed the action, or 'anonymousUser'
     */
    public void logAction(AuditLogAction action, String details, String username) {
        try {
            AuditMessage message = new AuditMessage(action, details, username);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.AUDIT_ROUTING_KEY, message);
        } catch (Exception e) {
            log.error("Failed to send audit message", e);
        }
    }
}
