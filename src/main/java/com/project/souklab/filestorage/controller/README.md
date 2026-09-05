# Storage Controller Package (`com.project.souklab.filestorage.controller`)

HTTP controllers for direct file streaming and access-controlled resource downloads.

---

## Endpoints

| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/files/{category}/{filename}` | Authenticated | Streams stored files from S3/MinIO with appropriate `Content-Type` headers and download rate limits. |

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`FileServingController`](FileServingController.java) | Streams stored file content directly to HTTP clients with ETag caching and mime detection. |
