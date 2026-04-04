package com.remit.mellonsecure.payout.consumer;

import com.remit.mellonsecure.payout.config.RabbitMQConfig;
import com.remit.mellonsecure.payout.service.WebhookDeliveryService;
import com.remit.mellonsecure.payout.publisher.WebhookPublisherAdapter.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookConsumer {

    private final WebhookDeliveryService webhookDeliveryService;

    @RabbitListener(queues = RabbitMQConfig.WEBHOOK_QUEUE)
    public void consume(WebhookMessage message) {
        log.info("Processing webhook: paymentReference={}, webhookUrl={}", message.paymentReference(), maskUrl(message.webhookUrl()));
        try {
            webhookDeliveryService.deliver(message);
        } catch (Exception e) {
            log.error("Webhook delivery failed: paymentReference={}", message.paymentReference(), e);
            throw new RuntimeException(e);
        }
    }

    private String maskUrl(String url) {
        if (url == null || url.length() < 20) return "***";
        return url.substring(0, 15) + "***";
    }
}
