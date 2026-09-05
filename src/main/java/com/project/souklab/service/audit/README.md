# Audit Service Package (`com.project.souklab.service.audit`)

System-wide administrative action logging and security trail persistence.

---

## Key Capabilities

- **Asynchronous Execution**: Audit log writes execute with `@Async` to prevent audit persistence latency from degrading critical request response times.
- **Caller Extraction**: Automatically extracts the authenticated actor from Spring Security's `SecurityContextHolder`.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`AuditLogService`](AuditLogService.java) | Logs actions (`APPROVE_USER`, `BAN_USER`, `TIMEOUT_USER`, `EMAIL_VERIFIED`) into `AuditLogRepository` and provides paginated queries for administrative reviews. |
