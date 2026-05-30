package com.erdv.service;

import com.erdv.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limite le nombre de requêtes POST sur les endpoints d'authentification (par adresse IP).
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/auth/login",
            "/auth/register",
            "/auth/forgot-password");

    @Value("${app.auth.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.auth.rate-limit.max-requests:20}")
    private int maxRequests;

    @Value("${app.auth.rate-limit.window-seconds:60}")
    private int windowSeconds;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<String, Window> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!enabled || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }

        if (!LIMITED_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientIp(request);
        if (isRateLimited(clientKey)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(),
                    ErrorResponse.of("Trop de tentatives. Réessayez dans quelques instants."));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String clientKey) {
        long now = System.currentTimeMillis();
        long windowMs = windowSeconds * 1000L;

        Window window = buckets.compute(clientKey, (key, existing) -> {
            if (existing == null || now - existing.windowStartMs >= windowMs) {
                return new Window(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return window.count.get() > maxRequests;
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static final class Window {
        private final long windowStartMs;
        private final AtomicInteger count;

        private Window(long windowStartMs, AtomicInteger count) {
            this.windowStartMs = windowStartMs;
            this.count = count;
        }
    }
}
