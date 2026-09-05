# Configuration Package (`com.project.souklab.config`)

Centralizes framework configurations, custom Spring Beans, security filters setup, asynchronous execution, and application property bindings.

---

## Key Responsibilities

- Configures Spring Security filter chains, authentication providers, and stateless session management.
- Sets up asynchronous task executors (`AsyncConfig`) and system clocks (`ClockConfig`) for deterministic testing.
- Binds externalized configuration properties (`AppProperties`, `AvatarProperties`).
- Configures WebSocket endpoints, STOMP message routing, and authentication handshakes.
- Seeds base administrative and testing data (`DataSeeder`).
- Integrates global API response status code customization (`ApiResponseCodeAdvice`).

---

## Classes Reference

| Class | Type | Responsibility |
| :--- | :--- | :--- |
| [`SecurityConfig`](SecurityConfig.java) | `@Configuration` | Configures `SecurityFilterChain`, CORS rules, public/protected endpoint permissions, and registers JWT and rate limit filters. |
| [`AppProperties`](AppProperties.java) | `@ConfigurationProperties(prefix = "app")` | Binds JWT secrets, expiration intervals, frontend URLs, and token configuration. |
| [`AvatarProperties`](AvatarProperties.java) | `@ConfigurationProperties(prefix = "app.avatar")` | Configures max avatar limits, allowed dimensions, and resolution tiers. |
| [`AsyncConfig`](AsyncConfig.java) | `@Configuration`, `@EnableAsync` | Configures thread pools for non-blocking operations (audit logging, email dispatch). |
| [`ClockConfig`](ClockConfig.java) | `@Configuration` | Exposes a `java.time.Clock` bean for time-dependent operations (token expiration, cooldown tracking). |
| [`PasswordEncoderConfig`](PasswordEncoderConfig.java) | `@Configuration` | Exposes a `BCryptPasswordEncoder` bean for secure credential hashing. |
| [`ApiResponseCodeAdvice`](ApiResponseCodeAdvice.java) | `@ControllerAdvice` | Intercepts HTTP response bodies to synchronize outer HTTP status codes with inner `ApiResponse.code`. |
| [`DataSeeder`](DataSeeder.java) | `@Component`, `CommandLineRunner` | Seeds initial roles (`ROLE_CLIENT`, `ROLE_ARTISAN`, `ROLE_ADMIN`) and bootstrap administrator accounts. |
| [`WebSocketConfig`](WebSocketConfig.java) | `@Configuration`, `@EnableWebSocketMessageBroker` | Configures STOMP messaging, `/ws` endpoint, user destination prefixes, and external broker relays. |
| [`WebSocketAuthInterceptor`](WebSocketAuthInterceptor.java) | `ChannelInterceptor` | Authenticates STOMP `CONNECT` frames by validating Bearer JWT tokens in connect headers. |
| [`WebClientConfig`](WebClientConfig.java) | Class | Foundation configuration class for external HTTP client integrations (e.g., Chargily Pay V2). |
