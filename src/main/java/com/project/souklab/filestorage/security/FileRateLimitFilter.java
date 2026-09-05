package com.project.souklab.filestorage.security;

import com.project.souklab.dto.common.ApiResponse;
import com.project.souklab.filestorage.config.StorageProperties;
import com.project.souklab.filestorage.controller.FileServingController;
import com.project.souklab.util.ServletResponseUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dedicated rate-limiting filter for file-serving and upload endpoints (/api/v1/files/**).
 * Operates independently from the global API rate limiter with configurable capacity and refill window.
 */
@Component
public class FileRateLimitFilter extends OncePerRequestFilter {

    private final ServletResponseUtil servletResponseUtil;
    private final StorageProperties.RateLimitProperties rateLimitProperties;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Constructs a new FileRateLimitFilter with injected response utility and storage properties.
     *
     * @param servletResponseUtil utility to write standard ApiResponse error envelopes to the servlet response
     * @param properties configuration properties containing file rate-limiting limits
     */
    public FileRateLimitFilter(ServletResponseUtil servletResponseUtil, StorageProperties properties) {
        this.servletResponseUtil = servletResponseUtil;
        this.rateLimitProperties = properties != null ? properties.getRateLimit() : new StorageProperties.RateLimitProperties();
    }

    /**
     * Determines whether the filter should be skipped for the given request.
     * Only requests targeting /api/v1/files/** are evaluated by this filter.
     *
     * @param request current HTTP request
     * @return true if request URI does not start with /api/v1/files, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.startsWith(FileServingController.BASE_PATH);
    }

    /**
     * Filters incoming file requests, consuming rate limit tokens and rejecting requests that exceed capacity with HTTP 429.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @param filterChain servlet filter chain
     * @throws ServletException in case of servlet processing errors
     * @throws IOException in case of I/O errors
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (rateLimitProperties != null && !rateLimitProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request);
        Bucket bucket = resolveBucket(key);

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            servletResponseUtil.writeResponse(
                    response,
                    HttpStatus.TOO_MANY_REQUESTS.value(),
                    ApiResponse.error("Too many requests. Please try again later.")
            );
        }
    }

    /**
     * Resolves the rate-limiting key for the request.
     * Authenticated users are keyed by username; unauthenticated callers are keyed by remote IP.
     *
     * @param request current HTTP request
     * @return unique bucket key
     */
    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return "user:" + auth.getName();
        }
        return "ip:" + request.getRemoteAddr();
    }

    /**
     * Resolves or creates a Bucket4j bucket for the specified client key.
     *
     * @param key unique bucket key
     * @return active Bucket instance
     */
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Creates a new Bucket instance initialized from the configured rateLimitProperties.
     *
     * @return new configured Bucket
     */
    private Bucket createNewBucket() {
        int capacity = (rateLimitProperties != null && rateLimitProperties.getCapacity() > 0)
                ? rateLimitProperties.getCapacity()
                : 120;
        Duration refillDuration = (rateLimitProperties != null && rateLimitProperties.getRefillDuration() != null)
                ? rateLimitProperties.getRefillDuration()
                : Duration.ofMinutes(1);

        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillDuration)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
