# S3 Client Storage Implementation (`com.project.souklab.filestorage.s3`)

Production-grade AWS SDK S3 client adapter compatible with MinIO, AWS S3, and Cloudflare R2.

---

## Classes Reference

| Class | Responsibility |
| :--- | :--- |
| [`S3StorageService`](S3StorageService.java) | Implements `StorageService` using AWS SDK v2 `S3Client`. Manages bucket creation, object puts with metadata, streaming object gets, and bulk deletes. |
