package com.futurescope.platform.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limits sensitive endpoints per client IP (and optionally per user when authenticated).
 * Applied to auth, interview submission, and proctoring paths.
 */
@Component
@Order(-100) // Run before JWT filter
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties properties;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private static final int MAX_CACHED_BUCKETS = 50_000;

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String clientKey = clientKey(request);
        LimitType type = limitType(path);

        if (type == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = bucketFor(type, clientKey);
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}");
        }
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private LimitType limitType(String path) {
        if (path == null) return null;
        if (path.startsWith("/auth/")) return LimitType.AUTH;
        if ("/interviews/start".equals(path)) return LimitType.INTERVIEW;
        if (path.matches("/interviews/[^/]+/questions/[^/]+/submit-code")) return LimitType.INTERVIEW;
        if (path.matches("/interviews/[^/]+/followups/[^/]+/answer")) return LimitType.INTERVIEW;
        if (path.matches(".*/proctoring/sessions/[^/]+/events")) return LimitType.PROCTORING_EVENTS;
        if (path.startsWith("/proctoring/sessions")) return LimitType.PROCTORING_SESSION;
        return null;
    }

    private Bucket bucketFor(LimitType type, String clientKey) {
        String key = type.name() + ":" + clientKey;
        return buckets.computeIfAbsent(key, k -> {
            if (buckets.size() >= MAX_CACHED_BUCKETS) {
                buckets.clear();
            }
            return createBucket(type);
        });
    }

    private Bucket createBucket(LimitType type) {
        int capacity;
        switch (type) {
            case AUTH -> capacity = properties.getAuthRequestsPerMinute();
            case INTERVIEW -> capacity = properties.getInterviewRequestsPerMinute();
            case PROCTORING_SESSION -> capacity = properties.getProctoringSessionRequestsPerMinute();
            case PROCTORING_EVENTS -> capacity = properties.getProctoringEventsPerMinute();
            default -> capacity = 60;
        }
        Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(capacity, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    private enum LimitType {
        AUTH,
        INTERVIEW,
        PROCTORING_SESSION,
        PROCTORING_EVENTS
    }
}
