# Data Access Object Layer (`com.project.souklab.dao`)

Spring Data JPA repository interfaces defining database queries, soft-delete filtering, and pagination contracts.

---

## Architecture & Data Flow

Repositories inherit from `JpaRepository` and `JpaSpecificationExecutor`. All queries touching entities with soft-delete support automatically include `deletedAt IS NULL` conditions to maintain data integrity.

```mermaid
graph TD
    Service["Service Layer"] --> Repo["Spring Data JPA Repository"]
    Repo --> Hibernate["Hibernate ORM 6"]
    Hibernate --> Hikari["HikariCP Connection Pool"]
    Hikari --> DB[("MariaDB / MySQL Database")]
```

---

## Repositories Reference

| Repository Interface | Managed Entity | Key Query Capabilities |
| :--- | :--- | :--- |
| [`UserRepository`](UserRepository.java) | `User` | `findByEmail`, `existsByEmail`, `findByStatusAndDeletedAtIsNull`, search query filters. |
| [`ArtisanRepository`](ArtisanRepository.java) | `Artisan` | `findById`, `findByTeacherTrue`, `findByVerifiedTrue`. |
| [`ClientRepository`](ClientRepository.java) | `Client` | `findById`, `findByPremiumTrue`. |
| [`RoleRepository`](RoleRepository.java) | `Role` | `findByName` (`ROLE_CLIENT`, `ROLE_ARTISAN`, `ROLE_ADMIN`). |
| [`NotificationRepository`](NotificationRepository.java) | `Notification` | `findByRecipientIdAndDeletedAtIsNullOrderByCreatedAtDesc`, `countByRecipientIdAndReadFalseAndDeletedAtIsNull`, query-scoped mark-read. |
| [`ArtisanFormateurRequestRepository`](ArtisanFormateurRequestRepository.java) | `ArtisanFormateurRequest` | `findFirstByArtisanIdOrderByCreatedAtDesc`, `findByStatusAndDeletedAtIsNullOrderByCreatedAtDesc`. |
| [`ArtisanProfileViewRepository`](ArtisanProfileViewRepository.java) | `ArtisanProfileView` | `existsByViewerIdAndArtisanId`, deduplicated profile view metrics. |
| [`UserAvatarRepository`](UserAvatarRepository.java) | `UserAvatar` | `findByUserIdOrderByCreatedAtDesc`, `countByUserId`, `findByUserIdAndIsActiveTrue`. |
| [`RefreshTokenRepository`](RefreshTokenRepository.java) | `RefreshToken` | `findByToken`, `deleteByUser`, revocation cleanup. |
| [`VerificationTokenRepository`](VerificationTokenRepository.java) | `VerificationToken` | `findActiveToken`, `invalidateActiveTokens` for email verification and password reset. |
| [`OAuthIdentityRepository`](OAuthIdentityRepository.java) | `OAuthIdentity` | `findByProviderAndProviderUserId`, OAuth account linking. |
| [`AuditLogRepository`](AuditLogRepository.java) | `AuditLog` | `findByActionOrderByCreatedAtDesc`, administrative audit queries. |
