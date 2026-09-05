# Storage Validation Package (`com.project.souklab.filestorage.validation`)

Pre-storage stream validation, magic bytes verification, and memory safety guards.

---

## Classes Reference

| Class | Type | Responsibility |
| :--- | :---: | :--- |
| [`FileValidator`](FileValidator.java) | Component | Inspects initial byte signatures (magic numbers) to verify true MIME types (JPEG, PNG, WebP) independent of declared file extensions. |
| [`SizeLimitingInputStream`](SizeLimitingInputStream.java) | `FilterInputStream` | Wraps incoming upload streams to throw `FileTooLargeException` immediately if bytes read exceed configured limits, preventing memory exhaustion. |
| [`ValidatedFile`](ValidatedFile.java) | Record | Value object encapsulating sanitized filename, verified content type, byte size, and reset input stream. |
