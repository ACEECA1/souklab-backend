# User & Avatar Controller Package (`com.project.souklab.controller.user`)

Handles administrative user moderation (approvals, bans, timeouts) and user avatar operations (upload, list, activate, delete).

---

## Endpoints

### User Moderation (`UserManagementController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/admin/users` | `ROLE_ADMIN` | Paginated search and filter across all users. |
| `GET` | `/api/v1/admin/users/pending` | `ROLE_ADMIN` | Lists users awaiting administrative validation. |
| `POST` | `/api/v1/admin/users/{id}/approve` | `ROLE_ADMIN` | Approves pending user, activates account, and sends notification. |
| `POST` | `/api/v1/admin/users/approve-bulk` | `ROLE_ADMIN` | Bulk approves a list of pending user IDs. |
| `POST` | `/api/v1/admin/users/{id}/ban` | `ROLE_ADMIN` | Permanently bans user, revokes refresh tokens, dispatches notification. |
| `POST` | `/api/v1/admin/users/{id}/timeout` | `ROLE_ADMIN` | Temporarily suspends user for specified duration in minutes. |
| `GET` | `/api/v1/admin/users/audit-logs` | `ROLE_ADMIN` | Queries platform administrative audit logs. |

### Avatar Gallery (`AvatarController`)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/users/me/avatar` | Authenticated | Uploads new avatar (enforces rate limit, ClamAV scan, magic bytes, resizing). |
| `GET` | `/api/v1/users/me/avatars` | Authenticated | Lists all gallery avatars owned by the authenticated user. |
| `PUT` | `/api/v1/users/me/avatars/{id}/activate` | Authenticated | Activates a gallery avatar as the primary profile avatar. |
| `DELETE` | `/api/v1/users/me/avatars/{id}` | Authenticated | Deletes an avatar record and associated storage files from S3/MinIO. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`UserManagementController`](UserManagementController.java) | Administrative approval, ban, timeout, and audit log endpoints. |
| [`AvatarController`](AvatarController.java) | User avatar upload, gallery retrieval, activation, and deletion. |
