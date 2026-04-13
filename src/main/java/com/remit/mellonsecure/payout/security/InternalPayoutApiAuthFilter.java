package com.remit.mellonsecure.payout.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Protects {@code /api/internal/**} with {@code X-Payout-Internal-Key} (shared with payment dashboard).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class InternalPayoutApiAuthFilter extends OncePerRequestFilter {

    @Value("${payout.internal-api-key:}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path == null || !path.startsWith("/api/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }
        String key = request.getHeader("X-Payout-Internal-Key");
        if (internalApiKey == null || internalApiKey.isBlank() || !internalApiKey.equals(key)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"message\":\"Unauthorized\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
