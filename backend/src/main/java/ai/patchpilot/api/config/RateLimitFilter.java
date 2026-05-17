package ai.patchpilot.api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate-limits POST /api/diagnose to 5 requests per IP per hour.
 * Protects the Claude API budget from bots and abuse.
 *
 * Buckets are stored in-memory per IP. Acceptable for portfolio scale;
 * for production at scale, consider Redis-backed buckets.
 */
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_HOUR = 5;
    private static final String RATE_LIMITED_PATH = "/api/diagnose";

    private final ConcurrentHashMap<String, Bucket> bucketsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Only rate-limit POST /api/diagnose. Everything else passes through.
        boolean isDiagnoseCall = "POST".equalsIgnoreCase(request.getMethod())
                && RATE_LIMITED_PATH.equals(request.getRequestURI());

        if (!isDiagnoseCall) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        Bucket bucket = bucketsByIp.computeIfAbsent(clientIp, ip -> newBucket());

        if (bucket.tryConsume(1)) {
            // Request allowed
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for IP {} on {}", clientIp, RATE_LIMITED_PATH);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                "{\"error\":\"Rate limit exceeded\"," +
                "\"message\":\"You can submit up to " + MAX_REQUESTS_PER_HOUR +
                " diagnoses per hour. Please try again later.\"}"
            );
        }
    }

    /**
     * Resolves the real client IP, respecting X-Forwarded-For for proxied
     * deployments (Render, Cloudflare). Falls back to remote address.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            // X-Forwarded-For can be "client, proxy1, proxy2" — take the first
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(MAX_REQUESTS_PER_HOUR)
                .refillIntervally(MAX_REQUESTS_PER_HOUR, Duration.ofHours(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}