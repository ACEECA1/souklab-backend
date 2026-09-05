# REST & Realtime API Specification

All endpoints are versioned with the `/api/v1` prefix. Standard response envelopes and HTTP status codes are consistently applied across the platform.

---

## 1. Response Envelopes & Error Handling

### Standard Response Format
```json
{
  "success": true,
  "code": 200,
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Paginated Response Format
```json
{
  "success": true,
  "code": 200,
  "message": "Data retrieved successfully",
  "data": {
    "content": [ ... ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 85,
    "totalPages": 5,
    "last": false
  }
}
```

### Error Response Formats

#### Business Exception Error (`AppException` subclasses)
```json
{
  "success": false,
  "code": 404,
  "errorCode": "RESOURCE_NOT_FOUND",
  "message": "User not found with id: 123",
  "data": null
}
```

#### Field Validation Error (`422 Unprocessable Entity`)
```json
{
  "success": false,
  "code": 422,
  "message": "Validation failed",
  "data": null,
  "errors": {
    "email": "Email is required",
    "password": "Password must be at least 8 characters long"
  }
}
```

#### Standard / System Error (`400`, `401`, `403`, `405`, `500`)
```json
{
  "success": false,
  "code": 400,
  "message": "Malformed or unreadable request body",
  "data": null
}
```

---

## 2. Authentication & Onboarding (`/api/v1/auth/**`)

### `POST /api/v1/auth/register`
Creates a base user account.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "artisan@example.com",
  "password": "StrongPassword123!",
  "name": "Ahmed Benali",
  "role": "ROLE_ARTISAN"
}
```
- **Response**: `201 Created` with User summary & confirmation email dispatch.

### `POST /api/v1/auth/login`
Authenticates credentials and returns JWT access + refresh tokens.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "artisan@example.com",
  "password": "StrongPassword123!"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "code": 200,
  "message": "Login successful.",
  "data": {
    "accessToken": "eyJhbGciOi...",
    "refreshToken": "7c9e6679-7425...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": "43fb36ad-7835-4fea-be7e-e3bc8f875e1e",
      "email": "artisan@example.com",
      "firstName": "Ahmed",
      "lastName": "Benali",
      "name": "Ahmed Benali",
      "phone": "+213 555 12 34 56",
      "avatarUrl": null,
      "accountStatus": "PENDING",
      "roles": [
        "ROLE_ARTISAN"
      ],
      "emailVerified": true,
      "emailVerifiedAt": "2026-09-01T10:00:00",
      "createdAt": "2026-09-01T10:00:00",
      "updatedAt": "2026-09-01T10:00:00",
      "bio": "Master ceramist specializing in traditional Kabyle and Islamic motifs.",
      "regionId": "reg-15",
      "city": "Tizi Ouzou",
      "address": "Route des Artisans, No. 12",
      "website": "https://artisan-example.dz",
      "subCategoryId": "subcat-pottery-01",
      "teacher": false,
      "verified": false,
      "premium": false,
      "rating": 0.0,
      "reviewsCount": 0
    },
    "roles": [
      "ROLE_ARTISAN"
    ]
  }
}
```

### `POST /api/v1/auth/refresh`
Rotates refresh tokens and generates a fresh access token.
- **Access**: Public
- **Request Body**: `{ "refreshToken": "7c9e6679-7425..." }`

### `POST /api/v1/auth/verify-email`
Verifies user email using a 6-digit verification code.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Email verified successfully.",
  "data": null
}
```

### `POST /api/v1/auth/resend-verification`
Resends an email verification code if the user exists and is unverified.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "user@example.com"
}
```
- **Response**: `200 OK` (Generic response to prevent user enumeration)
```json
{
  "success": true,
  "message": "If an unverified account exists for this email, a verification code has been sent.",
  "data": null
}
```

### `POST /api/v1/auth/forgot-password`
Initiates password reset by emailing a 6-digit reset code (or OAuth reminder notice).
- **Access**: Public
- **Request Body**:
```json
{
  "email": "user@example.com"
}
```
- **Response**: `200 OK` (Generic response to prevent user enumeration)
```json
{
  "success": true,
  "message": "If an account exists for this email, instructions have been sent.",
  "data": null
}
```

### `POST /api/v1/auth/reset-password`
Resets password using a 6-digit reset code and invalidates existing refresh tokens.
- **Access**: Public
- **Request Body**:
```json
{
  "email": "user@example.com",
  "code": "123456",
  "newPassword": "NewStrongPassword123!"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Password reset successfully. You can now log in with your new password.",
  "data": null
}
```

### `POST /api/v1/auth/change-password`
Changes the authenticated user's password and invalidates active refresh tokens.
- **Access**: Authenticated
- **Request Body**:
```json
{
  "oldPassword": "CurrentPassword123!",
  "newPassword": "NewStrongPassword456!"
}
```
- **Response**: `200 OK`
```json
{
  "success": true,
  "message": "Password changed successfully.",
  "data": null
}
```

### `POST /api/v1/auth/complete-profile`
Completes profile details for newly registered Artisans or Clients.
- **Access**: Authenticated
- **Artisan Request Body**:
```json
{
  "bio": "Master ceramist specializing in traditional Kabyle and Islamic motifs.",
  "region": "Tizi Ouzou",
  "city": "Beni Yenni",
  "address": "Route des Artisans, No. 12",
  "subCategoryId": "subcat-pottery-01",
  "materialIds": ["mat-clay-01", "mat-glaze-02"],
  "epoqueIds": ["epoque-berber-01"],
  "techniqueIds": ["tech-hand-turning-01"],
  "isTeacher": true
}
```

---

## 3. Public Directory & Search Engine (`/api/v1/public/directory/**`)

### `GET /api/v1/public/directory`
Full-text search and multi-facet filtering over active, verified artisans.
- **Access**: Public
- **Query Parameters**:
  - `q` (string, optional): Search keyword (e.g. `ceramique`, `cuir`, `Ahmed`)
  - `category` (string, optional): Category slug
  - `subcategory` (string, optional): Subcategory slug or ID
  - `region` (string, optional): Wilaya / Region slug
  - `material` (array of strings, optional): Material slugs
  - `epoque` (array of strings, optional): Historical era slugs
  - `technique` (array of strings, optional): Craft technique slugs
  - `featured` (boolean, optional): Filter premium/featured artisans
  - `page` (int, default: `0`), `size` (int, default: `12`), `sort` (string, default: `rating,desc`)
- **Response**: `200 OK` with paginated `ArtisanDirectoryCardDTO` list. Contact info (phone/email) is masked unless the requesting user has an active premium client subscription.

### `GET /api/v1/public/directory/{id}`
Returns complete artisan public dossier (Bio, Gallery, Certifications, Achievements, Reviews, Active Formations).

---

## 4. Catalog & Reference Taxonomy (`/api/v1/catalog/**`)

- `GET /api/v1/catalog/regions`: All wilayas and communes in hierarchical tree.
- `GET /api/v1/catalog/categories`: Categories with child subcategories.
- `GET /api/v1/catalog/materials`: Material families and individual crafting materials.
- `GET /api/v1/catalog/epoques`: Traditional and historical periods.
- `GET /api/v1/catalog/techniques`: Craftsmanship techniques.

---

## 5. Formations & Workshops (`/api/v1/formations/**`)

- `GET /api/v1/formations`: Browse published upcoming workshops.
- `GET /api/v1/formations/{id}`: Detailed curriculum and schedule.
- `POST /api/v1/formations`: Create course draft (`ROLE_ARTISAN`).
- `PUT /api/v1/formations/{id}`: Update curriculum.
- `POST /api/v1/formations/{id}/submit`: Submit course for admin review (`PENDING_REVIEW`).
- `POST /api/v1/formations/{id}/enroll`: Enroll in workshop (`ROLE_CLIENT`).

---

## 6. Social Feed, Reviews & Moderation

- `GET /api/v1/feed`: Paginated community posts (`FORMATION`, `ACTUALITE`, `ANNONCE`).
- `POST /api/v1/feed`: Create a post (`ROLE_ARTISAN` / `ROLE_ADMIN`).
- `POST /api/v1/client/reviews`: Submit a review for an artisan (Rating: 1-5, comment).
- `POST /api/v1/reports`: File abuse report on a user, post, or message.

---

## 7. Realtime Direct Messaging (`/api/v1/messages/**` & `/ws`)

### REST Endpoints
- `GET /api/v1/messages/conversations`: List user conversations with last message preview and unread count.
- `POST /api/v1/messages/conversations`: Initiate conversation with a user.
- `GET /api/v1/messages/conversations/{id}/messages`: Fetch paginated chat history.
- `POST /api/v1/messages/send`: Send a text message with optional media attachments.

### WebSocket STOMP Channels
- Handshake: `ws://localhost:8080/ws` (Pass `Authorization: Bearer <token>` in connect headers)
- Subscribe to personal messages: `/user/queue/messages`
- Subscribe to live notifications: `/user/queue/notifications`
- Send message: Destination `/app/chat.send`

---

## 8. Subscriptions & Chargily Pay V2 (`/api/v1/subscription/**`)

- `GET /api/v1/public/subscription/pricing`: Active platform tiers and DZD prices.
- `POST /api/v1/subscription/checkout`: Create a Chargily checkout session URL.
- `POST /api/v1/subscription/webhook`: Chargily payment notification listener with HMAC-SHA256 signature verification.

---

## 9. Admin & Moderation Operations (`/api/v1/admin/**`)

- `GET /api/v1/admin/users`: Paginated list of users with optional search filter.
- `GET /api/v1/admin/users/pending`: Paginated list of pending artisan registrations.
- `POST /api/v1/admin/users/{id}/approve`: Approve a user account (`ACTIVE`).
- `POST /api/v1/admin/users/approve-bulk`: Bulk approve multiple user accounts.
- `POST /api/v1/admin/users/{id}/ban`: Ban a user account with reason.
- `POST /api/v1/admin/users/{id}/timeout`: Timeout user for specified minutes with reason.
- `GET /api/v1/admin/stats`: KPI dashboard (total artisans, pending validations, active workshops, revenues).
- `POST /api/v1/admin/users/{id}/validation`: Approve, reject, or suspend artisan account.
- `POST /api/v1/admin/formations/{id}/review`: Approve or reject workshop curriculum.
- `POST /api/v1/admin/reports/{id}/resolve`: Resolve abuse reports and execute penalty actions.

---

## 10. Notifications (`/api/v1/notifications/**`)

- `GET /api/v1/notifications`: Paginated list of notifications for the authenticated user (`?page=0&size=20`).
- `GET /api/v1/notifications/unread-count`: Count of unread, non-deleted notifications.
- `PUT /api/v1/notifications/{id}/read`: Mark a specific notification as read.
- `PUT /api/v1/notifications/read-all`: Mark all notifications for the authenticated user as read.
- `DELETE /api/v1/notifications/{id}`: Soft-delete a notification for the authenticated user.
