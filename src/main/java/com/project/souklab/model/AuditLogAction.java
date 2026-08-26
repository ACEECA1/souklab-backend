package com.project.souklab.model;

public enum AuditLogAction {
    CREATE_ROLE,
    UPDATE_ROLE,
    ASSIGN_ROLE,
    ASSIGN_ROLE_BULK,
    APPROVE_USER,
    BAN_USER,
    TIMEOUT_USER,
    APPROVE_PASSWORD_RESET,
    REJECT_PASSWORD_RESET
}
