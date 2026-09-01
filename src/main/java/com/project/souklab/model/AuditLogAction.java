package com.project.souklab.model;

public enum AuditLogAction {
    // Authentication & Security
    EMAIL_VERIFIED,
    PASSWORD_RESET_COMPLETED,
    PASSWORD_CHANGED,

    // Role management
    ASSIGN_ROLE,
    ASSIGN_ROLE_BULK,

    // User & Account moderation
    APPROVE_USER,
    BAN_USER,
    TIMEOUT_USER,

    // Artisan Validation
    APPROVE_ARTISAN,
    REJECT_ARTISAN,

    // Formation Moderation
    APPROVE_FORMATION,
    REJECT_FORMATION,

    // Report Resolution
    RESOLVE_REPORT,
    DISMISS_REPORT
}
