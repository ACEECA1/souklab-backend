# Souklab Backend - Production Spring Boot System

Welcome to the backend architecture for **Souklab**, an enterprise-grade platform dedicated to the preservation, discovery, and commercialization of Algerian traditional crafts and craftsmanship heritage.

---

## 🎯 Project Vision & Context

Souklab connects three primary stakeholder groups:
1. **Artisans & Master Craftsmen**: Traditional artisans (pottery, leatherwork, jewelry, weaving, woodwork, brassware, etc.) who manage their profiles, showcase portfolios, publish educational masterclasses (*formations*), post community news, and monetize their skills.
2. **Clients & Connoisseurs**: Individuals and corporate clients seeking authentic craft products, hiring artisans for bespoke commissions, participating in workshops, and subscribing for verified artisan directories.
3. **Administrators & Curators**: Platform managers who review and validate artisan credentials, approve workshop curricula, moderate reviews and reports, and manage catalog taxonomies.

---

## 🛠 Technology Stack

- **Framework**: Spring Boot 3.x / 4.x (Java 17+)
- **Data Persistence**: Spring Data JPA + MySQL 8.0 (with connection pooling via HikariCP)
- **Full-Text Search**: Hibernate Search 8.x + Elasticsearch 8.x (Faceted multi-filter search)
- **Security & RBAC**: Spring Security 6, Stateless JWT (HS256/RS256), Refresh Token rotation, Bucket4j Rate Limiting
- **Realtime Communication**: Spring WebSocket (STOMP Broker) + JWT Handshake Interceptor
- **Payment Gateway**: Chargily Pay V2 API integration (Edahabia and CIB card processing in DZD)
- **File & Media Storage**: Local / S3-compatible storage with multi-part upload validation, PDFBox metadata handling, and ClamAV virus inspection

---

## 📁 Documentation Suite

Comprehensive technical documentation is maintained in the [`docs/`](./docs) folder:

| Document | Description |
| :--- | :--- |
| **[`docs/ARCHITECTURE.md`](./docs/ARCHITECTURE.md)** | Detailed system architecture, clean layering, security filters, STOMP WebSockets, and Elasticsearch indexing. |
| **[`docs/DATA_MODEL.md`](./docs/DATA_MODEL.md)** | Complete entity relational diagram, table schemas, relationships, enums, and JPA mapping rules. |
| **[`docs/API_SPEC.md`](./docs/API_SPEC.md)** | Full REST & WebSocket API specification with request/response payloads, query parameters, and error codes. |
| **[`docs/ROADMAP.md`](./docs/ROADMAP.md)** | Step-by-step modular implementation roadmap designed for iterative, guided development. |
| **[`CLAUDE.md`](./CLAUDE.md)** | Developer coding standards, conventions, and common CLI commands. |

---

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.8+ (or use `./mvnw`)
- MySQL 8.0+ running on `localhost:3306`
- Elasticsearch 8.x running on `localhost:9200` (optional for non-search tests)

### Local S3 Storage (MinIO)
Start the local S3-compatible storage container via Docker Compose:
```bash
docker compose up -d minio
```
- **S3 API Endpoint**: `http://localhost:9000` (used by Spring Boot backend)
- **MinIO Web Console**: `http://localhost:9001` (login: `minioadmin` / `minioadmin_secret` or configured `.env` values)
- **Health Check**: `curl -sI http://localhost:9000/minio/health/live`

### Build and Test
```bash
# Clean and compile
./mvnw clean compile

# Run tests
./mvnw test

# Package application
./mvnw clean package -DskipTests

# Run Spring Boot dev server
./mvnw spring-boot:run
```
