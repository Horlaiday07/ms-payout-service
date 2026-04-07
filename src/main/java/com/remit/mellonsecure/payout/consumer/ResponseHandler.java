package com.remit.mellonsecure.payout.consumer;

import com.remit.mellonsecure.payout.config.RabbitMQConfig;
import com.remit.mellonsecure.payout.entity.WebhookPayload;
import com.remit.mellonsecure.payout.service.MerchantLookupService;
import com.remit.mellonsecure.payout.repository.TransactionRepository;
import com.remit.mellonsecure.payout.publisher.WebhookPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseHandler {

    private final TransactionRepository transactionRepository;
    private final MerchantLookupService merchantLookupService;
    private final WebhookPublisher webhookPublisher;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.RESPONSE_QUEUE)
    public void handleResponse(TransferConsumer.ResponseMessage response) {
        log.info("Handling response: paymentReference={}, success={}",
                response.paymentReference(), response.success());

        String status = response.success() ? "SUCCESS" : "FAILED";
        String remarks = response.remarks() != null ? response.remarks() : "completed";

        transactionRepository.updateStatus(
                response.transactionId(),
                status,
                response.responseMessage(),
                response.processorReference()
        );

        merchantLookupService.findByMerchantIdFromCacheOrSync(response.merchantId()).ifPresent(merchant -> {
            if (merchant.getWebhookUrl() != null && !merchant.getWebhookUrl().isBlank()) {
                WebhookPayload payload = WebhookPayload.builder()
                        .merchantId(response.merchantId())
                        .paymentReference(response.paymentReference())
                        .merchantReference(response.merchantReference())
                        .processorReference(response.processorReference())
                        .status(status)
                        .responseCode(response.responseCode() != null ? response.responseCode() : "00")
                        .responseDescription(response.responseMessage() != null ? response.responseMessage() : "SUCCESS")
                        .amount(response.amount() != null ? response.amount() : BigDecimal.ZERO)
                        .remarks(remarks)
                        .webhookUrl(merchant.getWebhookUrl())
                        .build();
                webhookPublisher.publish(payload);
            }
        });

        if (response.success() && response.processorReference() != null && !response.processorReference().isBlank()) {
            QueryMessage queryMessage = new QueryMessage(response.transactionId(), response.processorReference());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYOUT_EXCHANGE,
                    "payout.query",
                    queryMessage
            );
        }
    }

    public record QueryMessage(String transactionId, String processorReference) {}
}
