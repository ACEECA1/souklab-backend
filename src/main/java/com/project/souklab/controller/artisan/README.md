# Artisan Controller Package (`com.project.souklab.controller.artisan`)

Handles HTTP endpoints for artisan public profile discovery and artisan self-service profile management.

---

## Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/artisans/{artisanId}` | Authenticated | Retrieves public view of an artisan profile with contact info gating and view tracking. |
| `PATCH` | `/api/v1/artisan/profile` | `ROLE_ARTISAN` | Partial updates to bio, address, website, craft subcategories, and techniques. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`ArtisanController`](ArtisanController.java) | REST controller mapping `/api/v1/artisans` and `/api/v1/artisan/profile`. Delegates to `ArtisanProfileService`. |
