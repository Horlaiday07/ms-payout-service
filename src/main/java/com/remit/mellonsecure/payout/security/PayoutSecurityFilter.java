package com.remit.mellonsecure.payout.security;

import com.remit.mellonsecure.payout.entity.MerchantStatus;
import com.remit.mellonsecure.payout.exception.MerchantNotFoundException;
import com.remit.mellonsecure.payout.exception.SignatureValidationException;
import com.remit.mellonsecure.payout.entity.Merchant;
import com.remit.mellonsecure.payout.repository.MerchantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
@Slf4j
public class PayoutSecurityFilter extends OncePerRequestFilter {

    private final MerchantRepository merchantRepository;

    @Value("${payout.signature.timestamp-tolerance-seconds:300}")
    private int timestampToleranceSeconds;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/v1/payout")) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrappedRequest = (ContentCachingRequestWrapper) request;

        try {
            String merchantId = wrappedRequest.getHeader("X-Merchant-Id");
            String apiKey = request.getHeader("X-API-KEY");
            String signature = request.getHeader("X-SIGNATURE");
            String timestamp = request.getHeader("X-TIMESTAMP");

            if (merchantId == null || merchantId.isBlank() || apiKey == null || apiKey.isBlank()) {
                sendUnauthorized(response, "Missing X-Merchant-Id or X-API-KEY");
                return;
            }

            Merchant merchant = merchantRepository.findById(merchantId)
                    .filter(m -> m.getApiKey().equals(apiKey))
                    .or(() -> merchantRepository.findByApiKey(apiKey).filter(m -> m.getId().equals(merchantId)))
                    .orElseThrow(() -> new MerchantNotFoundException(merchantId));

            if (merchant.getStatus() != MerchantStatus.ACTIVE) {
                sendUnauthorized(response, "Merchant is not active");
                return;
            }

            String clientIp = extractClientIp(wrappedRequest);
            if (!merchant.getWhitelistedIps().isEmpty() && !merchant.getWhitelistedIps().contains(clientIp)) {
                log.warn("IP not whitelisted: ip={}, merchantId={}", clientIp, merchantId);
                sendUnauthorized(response, "IP not whitelisted");
                return;
            }

            if (signature != null && !signature.isBlank() && timestamp != null && !timestamp.isBlank()) {
                cacheRequestBody(wrappedRequest);
                validateSignature(wrappedRequest, merchant.getSecretKey(), signature, timestamp);
            }

            MerchantContext.set(merchant);
            filterChain.doFilter(wrappedRequest, response);
        } catch (MerchantNotFoundException | SignatureValidationException e) {
            log.warn("Security validation failed: {}", e.getMessage());
            sendUnauthorized(response, e.getMessage());
        } finally {
            MerchantContext.clear();
        }
    }

    private void validateSignature(ContentCachingRequestWrapper request, String secretKey, String providedSignature, String timestamp)
            throws SignatureValidationException {
        long ts;
        try {
            ts = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            throw new SignatureValidationException("Invalid X-TIMESTAMP format");
        }
        long now = System.currentTimeMillis() / 1000;
        if (Math.abs(now - ts) > timestampToleranceSeconds) {
            throw new SignatureValidationException("Request timestamp expired (replay attack prevention)");
        }

        String body = getRequestBody(request);
        String payload = body + timestamp;
        String expectedSignature = computeHmacSha256(secretKey, payload);
        if (!expectedSignature.equalsIgnoreCase(providedSignature)) {
            throw new SignatureValidationException("Signature mismatch - request may be tampered");
        }
    }

    private void cacheRequestBody(ContentCachingRequestWrapper request) {
        try {
            request.getInputStream().readAllBytes();
        } catch (IOException ignored) {
        }
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        return buf.length > 0 ? new String(buf, StandardCharsets.UTF_8) : "";
    }

    private String computeHmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"responseCode\":\"401\",\"responseDescription\":\"" + message + "\"}");
    }
}
