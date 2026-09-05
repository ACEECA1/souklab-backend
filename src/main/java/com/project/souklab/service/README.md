# Service Layer Architecture (`com.project.souklab.service`)

Core transactional business logic, domain state transitions, and coordination layer.

---

## Architectural Principles

- **Transactional Boundaries**: Methods that alter database state declare `@Transactional`. Read-only query methods declare `@Transactional(readOnly = true)` to optimize Hibernate dirty-checking and JDBC connection usage.
- **Decoupled Responsibilities**: Services interact through dependency injection of other services, repositories, or event dispatchers. Controllers never perform direct database manipulation.
- **Audit & Notification Side-Effects**: Critical state transitions (account verification, accreditation status changes, user discipline) record audit log entries and emit in-app or email notifications.

```mermaid
graph TD
    Controller["Controller Layer"] --> Service["Application Services"]
    Service --> Repos["DAO / JPA Repositories"]
    Service --> Storage["File Storage Engine"]
    Service --> Notification["Notification Service"]
    Service --> Audit["Audit Log Service"]
    Service --> Email["Email Dispatch Helper"]
```

---

## Subpackages

| Subpackage | Domain Responsibility |
| :--- | :--- |
| [`artisan`](artisan/README.md) | Artisan profile updates, public profile sanitization, and contact gating. |
| [`audit`](audit/README.md) | Asynchronous system auditing and administrative audit log queries. |
| [`auth`](auth/README.md) | Credential verification, onboarding wizard, Spring Security user details. |
| [`formateur`](formateur/README.md) | Artisan teacher accreditation lifecycle, applications, and cooldown tracking. |
| [`notification`](notification/README.md) | In-app notification feeds, badge counter tracking, STOMP push broadcasts. |
| [`security`](security/README.md) | Cryptographic token management (verification tokens and refresh tokens). |
| [`user`](user/README.md) | Administrative moderation (approvals, bans, timeouts) and avatar gallery management. |
