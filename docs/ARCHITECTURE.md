# Architecture & Technical Blueprint

This document details the architectural design, security filters, data pipelines, and external integrations for the **Souklab** Spring Boot backend.

---

## 1. Clean Layered Architecture

The application adopts standard Clean/Layered Architecture with strict separation of concerns and dependency inversion:

```
[ HTTP Requests / WS Frames ]
              │
              ▼
   ┌───────────────────────┐
   │    Security Layer     │  ◄── RateLimitFilter, JwtAuthenticationFilter, WebSocketAuthInterceptor
   └──────────┬────────────┘
              │
              ▼
   ┌───────────────────────┐
   │   Controller Layer    │  ◄── REST Controllers, WebSocket STOMP Message Handlers
   └──────────┬────────────┘
              │ (DTOs / Request Models)
              ▼
   ┌───────────────────────┐
   │     Service Layer     │  ◄── Business Logic, Transaction Boundaries (@Transactional),
   │                       │      Event Publishing, External Gateway Clients (Chargily)
   └──────────┬────────────┘
              │ (Domain Entities)
              ▼
   ┌───────────────────────┐
   │ Persistence & Search  │  ◄── Spring Data JPA Repositories (MySQL)
   │         Layer         │      Hibernate Search / Elasticsearch Queries
   └───────────────────────┘
```

### Key Architectural Guidelines
- **Controllers** are thin: They validate incoming `@Valid` DTOs, extract `UserPrincipal` from SecurityContext, delegate to services, and wrap responses in `ApiResponse<T>`.
- **Services** own all business logic and invariants. Mutations are annotated with `@Transactional(rollbackFor = Exception.class)`.
- **Data Access** is strictly through Spring Data JPA interfaces (`JpaRepository`, `JpaSpecificationExecutor`).
- **DTOs** are isolated from Entities to prevent over-posting, lazy-loading serialization issues, and circular references.

---

## 2. Package Organization

Root package: `com.project.souklab`

```
com.project.souklab
├── config/                  # Framework & integration configs
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   ├── RabbitMQConfig.java
│   ├── AsyncConfig.java
│   ├── AppProperties.java
│   ├── ChargilyProperties.java
│   └── DataSeeder.java
│
├── security/                # Authentication & authorization infrastructure
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtils.java
│   ├── RateLimitFilter.java
│   ├── WebSocketAuthInterceptor.java
│   ├── UserPrincipal.java
│   └── CustomUserDetailsService.java
│
├── exception/               # Global error handling
│   ├── GlobalExceptionHandler.java
│   ├── AppException.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
│
├── util/                    # Reusable utilities
│   ├── SecurityUtils.java
│   ├── EmailUtil.java
│   ├── FileStorageUtil.java
│   ├── PdfImageUtil.java
│   ├── CodeGeneratorUtil.java
│   └── ChargilyClient.java
│
├── dto/                     # Transfer objects grouped by domain
│   ├── common/              # ApiResponse<T>, PageResponse<T>
│   ├── auth/                # LoginDTO, RegisterDTO, JwtResponseDTO, TokenRefreshDTO
│   ├── artisan/             # ProfileUpdateDTO, GalleryDTO, CertDTO, AchievementDTO
│   ├── catalog/             # CategoryDTO, RegionDTO, MaterialDTO, EpoqueDTO, TechniqueDTO
│   ├── directory/           # DirectorySearchFilterDTO, ArtisanDirectoryCardDTO
│   ├── formation/           # FormationCreateDTO, FormationReviewDTO, EnrollmentDTO
│   ├── feed/                # FeedPostCreateDTO, FeedPostResponseDTO
│   ├── message/             # MessageSendDTO, ConversationDTO, MessageResponseDTO
│   ├── payment/             # CheckoutRequestDTO, WebhookPayloadDTO, SubscriptionDTO
│   └── admin/               # ValidationActionDTO, ReportResolutionDTO, PlatformStatsDTO
│
├── model/                   # JPA Entities (extending BaseEntity)
│   ├── common/              # BaseEntity.java, AuditLog.java
│   ├── user/                # User.java, Role.java, Permission.java, RefreshToken.java, Client.java, ArtisanProfile.java
│   ├── catalog/             # Region.java, JobCategory.java, JobSubCategory.java, MaterialFamily.java, Material.java, Epoque.java, Technique.java
│   ├── artisan/             # ArtisanGalleryImage.java, ArtisanCertification.java, ArtisanAchievement.java, ArtisanSocialLink.java, ArtisanValidation.java
│   ├── formation/           # Formation.java, FormationEnrollment.java, FormationReview.java
│   ├── social/              # FeedPost.java, Conversation.java, ConversationParticipant.java, Message.java, Review.java, Report.java
│   └── payment/             # ClientSubscription.java, ArtisanSubscription.java, SubscriptionPricing.java, Payment.java, PaymentWebhookLog.java
│
├── dao/                     # JPA Repositories
│   ├── UserRepository.java
│   ├── ArtisanProfileRepository.java
│   ├── RegionRepository.java
│   ├── JobCategoryRepository.java
│   ├── FormationRepository.java
│   ├── MessageRepository.java
│   └── ...
│
├── service/                 # Service Interfaces & Implementations
│   ├── auth/                # AuthService, RefreshTokenService
│   ├── artisan/             # ArtisanProfileService, PortfolioService
│   ├── catalog/             # CatalogService, RegionService
│   ├── directory/           # DirectorySearchService
│   ├── formation/           # FormationService, FormationReviewService
│   ├── social/              # FeedService, ReviewService, ReportService
│   ├── message/             # MessageService, ConversationService
│   ├── notification/        # NotificationService (DB + STOMP push)
│   ├── payment/             # PaymentService, SubscriptionService, ChargilyWebhookHandler
│   └── admin/               # AdminDashboardService, AdminModerationService
│
└── controller/              # REST & STOMP Controllers
    ├── auth/                # AuthController (/api/v1/auth/**)
    ├── user/                # UserController, ClientController
    ├── artisan/             # ArtisanController (/api/v1/artisan/**)
    ├── catalog/             # CatalogController, RegionController
    ├── directory/           # DirectoryController (/api/v1/public/directory/**)
    ├── formation/           # FormationController (/api/v1/formations/**)
    ├── feed/                # FeedController (/api/v1/feed/**)
    ├── message/             # MessageController, MessageWebSocketController
    ├── notification/        # NotificationController
    ├── payment/             # SubscriptionController, WebhookController
    └── admin/               # AdminController (/api/v1/admin/**)
```

---

## 3. Security & Filter Pipeline

### Filter Sequence
```
Incoming HTTP Request
        │
        ▼
[ RateLimitFilter ] ────────── (Bucket4j: IP-based sliding window rate limit)
        │
        ▼
[ JwtAuthenticationFilter ] ── (Extracts Bearer token from header/cookie, validates claims, sets SecurityContext)
        │
        ▼
[ UsernamePasswordAuthenticationFilter ]
        │
        ▼
[ SecurityFilterChain Evaluation ]
        │
        ├── Permit All: /api/v1/auth/**, /api/v1/public/**, /ws/**, /error
        └── Authenticated: All domain endpoints guarded with @PreAuthorize("hasRole('...')")
```

### JWT Architecture
- **Access Token**: HMAC-SHA256 signature, 60-minute expiration. Contains `sub` (username/email), `userId`, `roles`, and `permissions`.
- **Refresh Token**: 30-day expiration, stored hashed in `refresh_tokens` table with revocation status and device metadata (`ipAddress`, `userAgent`).
- **Token Rotation**: Each refresh request revokes the existing refresh token and issues a new pair.

---

## 4. Full-Text Search with Hibernate Search & Elasticsearch

### Index Structure
- `ArtisanProfile` is the root `@Indexed` entity.
- Indexed fields include:
  - `@FullTextField`: `bio`, `user.name`, `city`, `address`
  - `@KeywordField`: `subCategory.id`, `subCategory.category.id`, `region.id`, `region.slug`
  - `@KeywordField`: `materials.material.slug`, `techniques.technique.slug`, `epoques.epoque.slug`
  - `@GenericField`: `rating`, `reviewsCount`, `isVerified`, `isPremium`, `isTeacher`

### Search Predicate Builder
The search service builds dynamic boolean queries:
1. **Match/Fuzzy**: Keyword search matching name, bio, craft terms.
2. **Filter**: Exact filters for wilaya, category, materials, techniques, and epoques.
3. **Sort**: Featured artisans boosted first, followed by average rating and view counts.

---

## 5. Realtime STOMP WebSocket Messaging

### Endpoints & Destinations
- **STOMP Connect Endpoint**: `/ws` (with SockJS fallback enabled).
- **Client Inbound Interceptor**: `WebSocketAuthInterceptor` extracts the JWT Bearer token from STOMP `CONNECT` headers and binds the `Principal`.
- **Destination Prefixes**:
  - `/app`: Inbound messages from clients (e.g. `/app/chat.send`).
  - `/topic`: Public/broadcast channels (e.g. `/topic/announcements`).
  - `/queue`: User-specific private queues (e.g. `/user/queue/messages`, `/user/queue/notifications`).

---

## 6. Chargily Pay V2 Integration Architecture

### Flow
1. **Checkout Initiation**: Client or Artisan initiates subscription upgrade via `POST /api/v1/subscription/checkout`.
2. **Session Creation**: Backend invokes Chargily Pay V2 API with amount, currency (DZD), customer metadata, and webhook callback URL.
3. **Redirection**: Chargily returns a `checkout_url` for the user to complete payment on the government-certified payment page.
4. **Webhook Receipt**: Chargily posts event payload (`checkout.paid` or `checkout.failed`) to `POST /api/v1/subscription/webhook`.
5. **Signature Verification**: Webhook handler calculates HMAC-SHA256 of raw request body using `CHARGILY_SECRET_KEY` and compares against `Signature` header.
6. **Idempotency & State Transition**: Checks `payment_webhook_logs`, updates `payments` and `artisan_subscriptions` / `client_subscriptions` to `ACTIVE`, and triggers a real-time notification.

---

## 7. Storage, Documents & Virus Protection

- **Storage Provider**: Abstracted `FileStorageService` with `LocalFileStorageServiceImpl` and `S3FileStorageServiceImpl`.
- **Upload Validation**: Maximum 10MB image upload, 25MB document upload. MIME types restricted to `image/jpeg`, `image/png`, `image/webp`, `application/pdf`.
- **Antivirus Scanning**: Integrates with ClamAV daemon via TCP socket (`clamav-client`). Files failing scan are immediately quarantined and rejected.
- **PDFBox Processing**: Thumbnail generation for certificates and portfolio documents.
