package com.remit.mellonsecure.payout.publisher;

import com.remit.mellonsecure.payout.config.RabbitMQConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remit.mellonsecure.payout.entity.WebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookPublisherAdapter implements WebhookPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(WebhookPayload payload) {
        try {
            var dto = com.remit.mellonsecure.payout.dto.WebhookPayloadDto.builder()
                    .responseCode(payload.getResponseCode())
                    .responseDescription(payload.getResponseDescription())
                    .merchantReference(payload.getMerchantReference())
                    .paymentReference(payload.getPaymentReference())
                    .processorReference(payload.getProcessorReference())
                    .amount(payload.getAmount())
                    .remarks(payload.getRemarks())
                    .status(payload.getStatus())
                    .build();
            String json = objectMapper.writeValueAsString(dto);
            WebhookMessage message = new WebhookMessage(
                    payload.getMerchantId(),
                    payload.getPaymentReference(),
                    payload.getMerchantReference(),
                    payload.getWebhookUrl(),
                    json
            );
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYOUT_EXCHANGE,
                    "payout.webhook",
                    message
            );
            log.info("Published webhook: paymentReference={}", payload.getPaymentReference());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize webhook payload", e);
            throw new RuntimeException("Webhook serialization failed", e);
        }
    }

    public record WebhookMessage(String merchantId, String paymentReference, String merchantReference, String webhookUrl, String payload) {}
}
