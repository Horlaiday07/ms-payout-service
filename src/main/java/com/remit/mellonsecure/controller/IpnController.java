package com.remit.mellonsecure.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ipn")
public class IpnController {

    private static final Logger log = LoggerFactory.getLogger(IpnController.class);

    @PostMapping
    public ResponseEntity<Map<String, String>> handleIpn(
            @RequestBody(required = false) String body,
            HttpServletRequest request) {

        String clientIp = extractClientIp(request);
        String correlationId = request.getHeader("X-Correlation-Id");
        log.info("IPN received from IP={}, correlationId={}", clientIp, correlationId);

        return ResponseEntity.ok(Map.of(
                "status", "received",
                "message", "IPN processed successfully"
        ));
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
