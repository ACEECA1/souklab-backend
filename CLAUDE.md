# Souklab Backend - Spring Boot Production Architecture

Production-grade Spring Boot 3/4 backend powering the **Souklab** Algerian Artisan & Craftsmanship platform, derived from the project template and Next.js MVP specifications.

## 🛠 Tech Stack
- **Language**: Java 17+
- **Framework**: Spring Boot 3.x / 4.x (Web, Security, Data JPA, Validation, WebSocket, AMQP)
- **Database**: PostgreSQL / MySQL (JPA/Hibernate + Flyway/Liquibase or Hibernate DDL)
- **Search Engine**: Hibernate Search + Elasticsearch (Full-text directory & catalog search)
- **Messaging & Realtime**: Spring WebSocket (STOMP) / Pusher / RabbitMQ
- **Security**: Spring Security 6 (Stateless JWT auth, Refresh Tokens, Bucket4j Rate Limiting, RBAC)
- **Payments**: Chargily Pay V2 API integration (Edahabia & CIB cards)
- **Storage**: Local/S3/MinIO file storage with ClamAV virus scanning & PDFBox processing

## 📁 Package Architecture (`com.project.souklab` or `com.souklab`)
```
src/main/java/com/project/souklab/
├── config/              # SecurityConfig, WebSocketConfig, RabbitMQConfig, AsyncConfig, AppProperties
├── security/            # JwtAuthenticationFilter, JwtUtils, RateLimitFilter, CustomUserDetails
├── controller/          # REST endpoints organized by domain
│   ├── auth/            # Authentication & Account completion (/api/v1/auth/**)
│   ├── artisan/         # Artisan profile, portfolio, gallery, certs, stats (/api/v1/artisan/**)
│   ├── catalog/         # Categories, Subcategories, Materials, Epoques, Techniques, Regions (/api/v1/catalog/**)
│   ├── directory/       # Public artisan directory search & filtering (/api/v1/public/directory/**)
│   ├── formation/       # Workshop courses, enrollments & reviews (/api/v1/formations/**)
│   ├── feed/            # Feed posts (Actualité, Formation, Annonce) (/api/v1/feed/**)
│   ├── message/         # Conversations & direct messaging (/api/v1/messages/**)
│   ├── subscription/    # Plans, pricing & Chargily checkout (/api/v1/subscription/**)
│   └── admin/           # Admin validation, moderation, stats, catalog CRUD (/api/v1/admin/**)
├── model/               # JPA Entities extending BaseEntity
│   ├── user/            # User, Role, Permission, Client, ArtisanProfile
│   ├── catalog/         # Region, JobCategory, JobSubCategory, Material, MaterialFamily, Epoque, Technique
│   ├── formation/       # Formation, FormationEnrollment, FormationReview
│   ├── social/          # FeedPost, Conversation, ConversationParticipant, Message, Review, Report
│   └── payment/         # ClientSubscription, ArtisanSubscription, SubscriptionPricing, Payment, PaymentWebhookLog
├── dao/                 # Spring Data JPA Repositories
├── service/             # Business logic layer with interface-implementation separation
├── dto/                 # Request & Response DTOs mapped with validation annotations
├── exception/           # GlobalExceptionHandler & domain AppException
└── util/                # FileStorageUtil, SecurityUtils, EmailUtil, CodeGeneratorUtil
```

## 🏗 Coding Conventions
- **Entity Base**: All entities inherit from `BaseEntity` (`id`, `createdAt`, `updatedAt`).
- **Response Wrapper**: All controller responses return standard `ApiResponse<T>` with `success`, `message`, `data`, and pagination metadata where applicable.
- **DTO Validation**: Use Jakarta Validation annotations (`@NotNull`, `@NotBlank`, `@Size`, `@Email`) on all request DTOs.
- **Security & RBAC**: Stateless JWT auth with `@PreAuthorize("hasRole('ADMIN')")` or permission-based evaluations.
- **Transactions**: Annotate service mutation methods with `@Transactional`.
- **Soft Deletes & Audit**: Track `deletedAt`, `createdBy`, `updatedBy` for core domain entities.

## 🚀 Build & Run
- **Build**: `./mvnw clean package -DskipTests`
- **Run Locally**: `./mvnw spring-boot:run`
- **Run Tests**: `./mvnw test`
- **Profiles**: `application-dev.properties`, `application-prod.properties`
