package com.remit.mellonsecure.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        String clientIp = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();

        String headersParam = Collections.list(httpRequest.getHeaderNames()).stream()
                .map(name -> name + "=" + maskSensitiveHeader(name, httpRequest.getHeader(name)))
                .collect(Collectors.joining(", "));

        log.info("Request: method={}, path={}, query={}, ip={}, userAgent={}, headers=[{}]",
                method, path, queryString, clientIp, userAgent, headersParam);

        chain.doFilter(request, response);
    }

    private String maskSensitiveHeader(String name, String value) {
        if (value == null || value.isEmpty()) return value;
        String lower = name.toLowerCase();
        if (lower.equals("authorization") || lower.equals("cookie") || lower.equals("x-api-key")) {
            return "***";
        }
        return value;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        if (request.getRemoteAddr() != null) {
            return request.getRemoteAddr();
        }
        return "unknown";
    }
}
