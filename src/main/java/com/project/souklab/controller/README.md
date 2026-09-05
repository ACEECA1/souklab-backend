# Controller Layer (`com.project.souklab.controller`)

HTTP adapter layer exposing RESTful endpoints according to API conventions.

---

## Architectural Principles

- **Thin Controllers**: Controllers contain no business logic; they validate incoming requests, delegate to application services, and wrap results in standard `ApiResponse` or `PaginatedResponse` wrappers.
- **Consistent Envelopes**: All successful responses return `ApiResponse<T>`, and all paginated list endpoints return `ApiResponse<PaginatedResponse<T>>`.
- **Security Scoping**: Endpoints enforce role-based authorization via Spring Security annotations (`@PreAuthorize("hasRole('ADMIN')")`, `@PreAuthorize("hasRole('ARTISAN')")`).
- **Input Validation**: Request bodies are validated using `@Valid` and Jakarta constraints.

```mermaid
graph LR
    HTTP["HTTP Request"] --> Controller["REST Controller"]
    Controller --> Validation["Jakarta Validation"]
    Controller --> Service["Transactional Service Layer"]
    Service --> ResponseDTO["Response DTO"]
    ResponseDTO --> Envelope["ApiResponse<T>"]
    Envelope --> Client["HTTP 200/201/204"]
```

---

## Subpackages

| Subpackage | Purpose |
| :--- | :--- |
| [`artisan`](artisan/README.md) | Artisan profile updates, public profile viewing, portfolio management. |
| [`auth`](auth/README.md) | Registration, login, email verification, token refreshing, password management. |
| [`formateur`](formateur/README.md) | Formateur teacher accreditation applications, administrative review, grant, and revocation. |
| [`notification`](notification/README.md) | User in-app notification queries, unread counts, mark-read, and soft deletion. |
| [`user`](user/README.md) | Administrative user management, timeouts, bans, and user avatar gallery operations. |
