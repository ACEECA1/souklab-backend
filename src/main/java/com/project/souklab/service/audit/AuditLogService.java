package com.project.souklab.service.audit;

import com.project.souklab.dao.AuditLogRepository;
import com.project.souklab.dao.UserRepository;
import com.project.souklab.model.AuditLog;
import com.project.souklab.model.AuditLogAction;
import com.project.souklab.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Records an audit log entry for a specific action performed by a user.
     * If the current user is authenticated, their username is attached to the log.
     * Otherwise, the action is logged under 'anonymousUser'.
     *
     * @param action the {@link AuditLogAction} representing the action type
     * @param details a detailed description of the action and its context
     */
    @Async("applicationTaskExecutor")
    @Transactional
    public void logAction(AuditLogAction action, String details) {
        String username = SecurityUtils.getCurrentUsername();
        String effectiveUsername = (username != null && !username.equals("anonymousUser")) ? username : "anonymousUser";
        logAction(action, details, effectiveUsername);
    }

    /**
     * Internal async worker to persist the audit log.
     *
     * @param action the {@link AuditLogAction} representing the action type
     * @param details a detailed description of the action
     * @param username the username associated with this action
     */
    @Async("applicationTaskExecutor")
    @Transactional
    public void logAction(AuditLogAction action, String details, String username) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setDetails(details);

            if (username != null && !username.equals("anonymousUser")) {
                userRepository.findByEmail(username).ifPresent(auditLog::setUser);
            }

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to record audit log entry for action: {}", action, e);
        }
    }
}
