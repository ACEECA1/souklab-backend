# Artisan Service Package (`com.project.souklab.service.artisan`)

Business logic for artisan public profiles, contact details gating, and deduplicated impression metrics.

---

## Key Capabilities

- **Contact Info Gating**: Public views of artisan profiles mask contact details (phone, email, website, physical address) unless the viewer is an administrator, the artisan themselves, or an active client with a premium subscription.
- **Impression Tracking**: Tracks profile visits in `ArtisanProfileView`, ensuring view counts only increment once per unique viewer-artisan pair.
- **Profile Patching**: Sanitizes and partially applies biography, website, city, and craft taxonomy updates.

---

## Classes Reference

| Service Class | Responsibility |
| :--- | :--- |
| [`ArtisanProfileService`](ArtisanProfileService.java) | Implements `getArtisanPublicView(artisanId, viewer)` and `patchProfile(artisanUser, patchDTO)`. |
