# Storage Security Package (`com.project.souklab.filestorage.security`)

Protective filters preventing denial-of-service bursting on file serving endpoints.

---

## Classes Reference

| Filter | Responsibility |
| :--- | :--- |
| [`FileRateLimitFilter`](FileRateLimitFilter.java) | `OncePerRequestFilter` applying token bucket limits to `/api/v1/files/**` download requests to prevent bandwidth exhaustion attacks. |
