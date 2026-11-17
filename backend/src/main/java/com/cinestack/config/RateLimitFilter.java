package com.cinestack.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Limits how often one IP address may hit the endpoints that are open to anyone and
 * costly to abuse: login and register (password guessing, mass sign-ups) and search
 * (every call spends the shared TMDB quota).
 *
 * Behind a reverse proxy, getRemoteAddr() is the proxy's address. Configure forwarded
 * headers at deploy time so this sees the real client.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private final FixedWindowLimiter limiter = new FixedWindowLimiter();
    private final int authPerMinute;
    private final int searchPerMinute;

    public RateLimitFilter(@Value("${app.rate-limit.auth-per-minute:10}") int authPerMinute,
                           @Value("${app.rate-limit.search-per-minute:30}") int searchPerMinute) {
        this.authPerMinute = authPerMinute;
        this.searchPerMinute = searchPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String bucket = null;
        int limit = 0;
        if ("POST".equals(request.getMethod()) && (path.equals("/api/auth/login") || path.equals("/api/auth/register"))) {
            bucket = "auth";
            limit = authPerMinute;
        } else if ("GET".equals(request.getMethod()) && path.equals("/api/movies/search")) {
            bucket = "search";
            limit = searchPerMinute;
        }

        if (bucket != null && !limiter.tryAcquire(bucket + ":" + request.getRemoteAddr(), limit, System.currentTimeMillis())) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\","
                    + "\"message\":\"Too many requests. Wait a minute and try again.\",\"fields\":{}}");
            return;
        }
        chain.doFilter(request, response);
    }
}
