# Security Token Services (`com.project.souklab.service.security`)

Cryptographic token management for long-lived sessions and one-time verification actions.

---

## Key Capabilities

- **Refresh Token Lifecycle**: Generates cryptographically random UUID tokens stored in `RefreshToken` entities with configured expiration. Supports revocation on logout or account ban.
- **Verification Tokens**: Generates 6-digit numeric OTP tokens for email activation and password resets. Hashes tokens before persistence and invalidates existing active tokens upon reissue.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`RefreshTokenService`](RefreshTokenService.java) | Token issuance, rotation, expiration verification, and revocation cleanup. |
| [`VerificationTokenService`](VerificationTokenService.java) | OTP generation, cryptographic hashing, expiration verification, and single-use invalidation. |
