# User & Avatar Service Package (`com.project.souklab.service.user`)

Handles administrative user discipline and full-lifecycle avatar processing.

---

## Key Capabilities

### 1. User Moderation
- **Approvals**: Activates pending accounts (`AccountStatus.ACTIVE`), flags artisans as verified, and emits `ACCOUNT_VALIDATED` notifications.
- **Bans**: Suspends account indefinitely (100-year horizon), records administrative reason, and revokes all active refresh tokens.
- **Timeouts**: Temporarily suspends account for specified duration in minutes and invalidates active tokens.

### 2. Avatar Processing Pipeline
- Validates file headers, executes ClamAV virus scanning, generates 3 resolution tiers (`THUMBNAIL` 150x150, `MEDIUM` 500x500, `FULL` original), uploads to MinIO/S3, and tracks gallery items in `UserAvatar`.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`UserManagementService`](UserManagementService.java) | Implements user search, pending list retrieval, single/bulk approvals, bans, and timeouts. |
| [`AvatarService`](AvatarService.java) | Orchestrates avatar uploads, gallery limits (max 5 per user), primary avatar activation, and file deletion. |
