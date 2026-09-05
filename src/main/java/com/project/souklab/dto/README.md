# Data Transfer Objects (`com.project.souklab.dto`)

Contracts and schemas defining client-server communications across the application.

---

## Architectural Principles

- **No Entity Leaks**: JPA persistence entities (`User`, `Artisan`, `Notification`) are never exposed directly via REST responses to avoid mass assignment vulnerabilities and accidental data leakage.
- **Strict Typing**: All payload structures use typed Java records or Lombok-annotated classes with Jackson serialization hints.
- **Jakarta Validation**: Inbound DTOs enforce constraint annotations (`@NotBlank`, `@Email`, `@Size`, `@Pattern`).

---

## Subpackages

| Package | Purpose |
| :--- | :--- |
| [`admin`](admin/README.md) | Administrative audit log representations. |
| [`auth`](auth/README.md) | User registration, login credentials, password resets, and JWT responses. |
| [`common`](common/README.md) | Standard API envelopes (`ApiResponse<T>`) and pagination wrappers (`PaginatedResponse<T>`). |
| [`formateur`](formateur/README.md) | Accreditation applications, approval notes, rejections, and cooldown data. |
| [`notification`](notification/README.md) | Notification item feed responses. |
| [`profile`](profile/README.md) | Artisan public profile views, client representations, and patch updates. |
| [`user`](user/README.md) | User moderation requests (bans, timeouts) and avatar representations. |
