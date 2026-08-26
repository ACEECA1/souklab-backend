package com.project.souklab.service.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.project.souklab.config.RabbitMQConfig;
import com.project.souklab.dao.AuditLogRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.dto.audit.AuditMessage;
import com.project.souklab.model.AuditLog;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditConsumer {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Consumes an audit message from the RabbitMQ audit queue.
     * Maps the received message to an {@link AuditLog} entity and saves it to the database.
     * Associates the action with a user if the username provided in the message exists.
     *
     * @param message the {@link AuditMessage} containing the action, details, and username
     */
    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    @Transactional
    public void consumeAuditMessage(AuditMessage message) {
        log.info("Received AuditMessage: {}", message);
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(message.getAction());
            auditLog.setDetails(message.getDetails());
            
            userRepository.findByUsername(message.getUsername()).ifPresent(auditLog::setUser);
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to process audit message", e);
        }
    }
}
