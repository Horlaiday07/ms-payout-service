package com.remit.mellonsecure.payout.service;

import com.remit.mellonsecure.payout.publisher.WebhookPublisherAdapter.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryService {

    private final MerchantLookupService merchantLookupService;
    private final WebClient.Builder webClientBuilder;

    @Value("${payout.webhook.signature-header:X-Webhook-Signature}")
    private String signatureHeader;

    @Value("${payout.webhook.timeout-ms:10000}")
    private int timeoutMs;

    public void deliver(WebhookMessage message) {
        merchantLookupService.findByMerchantIdFromCacheOrSync(message.merchantId())
                .ifPresent(merchant -> {
                    String signature = computeHmacSha256(merchant.getSecretKey(), message.payload());
                    deliverToUrl(message.webhookUrl(), message.payload(), signature);
                });
    }

    private void deliverToUrl(String url, String payload, String signature) {
        try {
            webClientBuilder.build()
                    .post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(signatureHeader, signature)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block(java.time.Duration.ofMillis(timeoutMs));
            log.info("Webhook delivered successfully: url={}", maskUrl(url));
        } catch (Exception e) {
            log.error("Webhook delivery failed: url={}", maskUrl(url), e);
            throw new RuntimeException("Webhook delivery failed: " + e.getMessage());
        }
    }

    private String computeHmacSha256(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    private String maskUrl(String url) {
        if (url == null || url.length() < 20) return "***";
        return url.substring(0, 15) + "***";
    }
}
