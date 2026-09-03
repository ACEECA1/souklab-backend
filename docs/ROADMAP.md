# Step-by-Step Implementation Roadmap

This roadmap breaks down the development of the **Souklab** production Spring Boot backend into atomic, verifiable steps. We will implement these step by step with user guidance.

---

## 📍 Phase 1: Build Infrastructure & Core Foundation
- [ ] **Step 1.1**: Clean up and optimize `pom.xml` (Spring Boot 3.3.x / 4.x, Java 17, Spring Data JPA, Spring Security, MySQL Connector, JJWT, Bucket4j, Hibernate Search / Elasticsearch, Spring WebSocket STOMP, Jakarta Validation).
- [ ] **Step 1.2**: Configure `application.properties` and `application-dev.properties` (Datasource connection pool, Hibernate DDL, Elasticsearch URIs, JWT secret, CORS policies, Chargily Pay properties).
- [ ] **Step 1.3**: Implement Core Utilities & Foundation (`BaseEntity`, `ApiResponse<T>`, `PageResponse<T>`, `AppException`, `GlobalExceptionHandler`).
- [ ] **Step 1.4**: Compile and verify base setup with `./mvnw clean compile`.

---

## 📍 Phase 2: Security, Authentication & Identity Management
- [ ] **Step 2.1**: Implement `User`, `Role`, `Permission`, and `RefreshToken` JPA entities and repositories.
- [ ] **Step 2.2**: Implement `JwtUtils`, `JwtAuthenticationFilter`, `RateLimitFilter` (Bucket4j), and `CustomUserDetailsService`.
- [ ] **Step 2.3**: Configure `SecurityConfig` (stateless session, route whitelisting, CORS bean, method security).
- [ ] **Step 2.4**: Implement `AuthService` and `AuthController` (`/api/v1/auth/register`, `/login`, `/refresh`, `/me`).
- [ ] **Step 2.5**: Write unit and integration tests for authentication workflows.

---

## 📍 Phase 3: Catalog, Taxonomy & Heritage Reference Data
- [ ] **Step 3.1**: Create JPA entities: `Region`, `JobCategory`, `JobSubCategory`, `MaterialFamily`, `Material`, `Epoque`, `Technique`.
- [ ] **Step 3.2**: Create Spring Data repositories with caching annotations.
- [ ] **Step 3.3**: Implement `CatalogService` & `CatalogController` (`/api/v1/catalog/**`).
- [ ] **Step 3.4**: Implement `DataSeeder` with complete Algerian wilayas, traditional craft categories, material families, and historical periods.

---

## 📍 Phase 4: Artisan & Client Profiles
- [ ] **Step 4.1**: Implement `Artisan`, `Client`, `ArtisanGalleryImage`, `ArtisanCertification`, `ArtisanAchievement`, `ArtisanSocialLink`, and join tables.
- [ ] **Step 4.2**: Implement multi-step `/api/v1/auth/complete-profile` for artisans and clients.
- [ ] **Step 4.3**: Implement `ArtisanService` and `ArtisanController` for portfolio, gallery, and certification management.
- [ ] **Step 4.4**: Implement secure multi-part file upload with MIME validation and ClamAV antivirus inspection.

---

## 📍 Phase 5: Elasticsearch Indexing & Public Directory Search
- [ ] **Step 5.1**: Add Hibernate Search annotations (`@Indexed`, `@FullTextField`, `@KeywordField`) on `Artisan` and related entities.
- [ ] **Step 5.2**: Implement `DirectorySearchService` using Hibernate Search MassIndexer and boolean query builder.
- [ ] **Step 5.3**: Implement `DirectoryController` (`/api/v1/public/directory`) supporting multi-criteria filtering (wilaya, category, material, era, technique, keyword, featured, rating).
- [ ] **Step 5.4**: Add contact data masking logic for free vs. premium client access.

---

## 📍 Phase 6: Formations (Workshops / Masterclasses)
- [ ] **Step 6.1**: Implement `Formation`, `FormationEnrollment`, `FormationReview` entities and repositories.
- [ ] **Step 6.2**: Implement approval state machine: `DRAFT` → `PENDING_REVIEW` → `APPROVED`/`REJECTED` → `PUBLISHED`.
- [ ] **Step 6.3**: Implement `FormationService` and `FormationController` (creation, curriculum update, review submission, client enrollment).

---

## 📍 Phase 7: Social Feed, Reviews & Moderation
- [ ] **Step 7.1**: Implement `FeedPost` (Actualité, Formation, Annonce) with multi-image attachments.
- [ ] **Step 7.2**: Implement `Review` system with automatic recalculation of artisan average rating and review counts.
- [ ] **Step 7.3**: Implement `Report` system for flagging abuse on users, messages, and posts.

---

## 📍 Phase 8: Realtime Direct Messaging & Notifications
- [ ] **Step 8.1**: Configure Spring STOMP WebSocket (`/ws` endpoint, `/topic`, `/queue`, `/app`, `/user`).
- [ ] **Step 8.2**: Implement `WebSocketAuthInterceptor` to validate JWT in STOMP headers on connect.
- [ ] **Step 8.3**: Implement `Conversation`, `ConversationParticipant`, `Message` entities and repositories.
- [ ] **Step 8.4**: Implement `MessageService` with read receipts and realtime STOMP dispatching.
- [ ] **Step 8.5**: Implement in-app notification engine with STOMP broadcasting.

---

## 📍 Phase 9: Monetization, Subscriptions & Chargily Pay V2
- [ ] **Step 9.1**: Implement `SubscriptionPricing`, `ArtisanSubscription`, `ClientSubscription`, `Payment`, `PaymentWebhookLog`.
- [ ] **Step 9.2**: Build `ChargilyClient` for checkout session creation.
- [ ] **Step 9.3**: Implement secure webhook handler (`POST /api/v1/subscription/webhook`) with HMAC-SHA256 signature verification and idempotency.
- [ ] **Step 9.4**: Implement automated subscription lifecycle manager (renewal, expiry cron job).

---

## 📍 Phase 10: Admin Dashboard & Production Hardening
- [ ] **Step 10.1**: Implement `AdminService` and `AdminController` (artisan validation queue, formation reviews, report resolutions).
- [ ] **Step 10.2**: Implement platform analytics & KPI aggregation (`/api/v1/admin/stats`).
- [ ] **Step 10.3**: Actuator monitoring, health checks, rate-limit fine-tuning, and Swagger/OpenAPI documentation.
- [ ] **Step 10.4**: End-to-end integration test suite.
