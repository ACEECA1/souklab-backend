package com.project.souklab.model;

public enum AuditLogAction {
    // Role management
    CREATE_ROLE,
    UPDATE_ROLE,
    DELETE_ROLE,
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
