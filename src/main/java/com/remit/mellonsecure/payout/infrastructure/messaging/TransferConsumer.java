package com.remit.mellonsecure.payout.infrastructure.messaging;

import com.remit.mellonsecure.payout.domain.model.TransferMessage;
import com.remit.mellonsecure.payout.domain.model.TransferRequest;
import com.remit.mellonsecure.payout.domain.model.TransferResult;
import com.remit.mellonsecure.payout.domain.port.ProcessorAdapter;
import com.remit.mellonsecure.payout.domain.port.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferConsumer {

    private final ProcessorAdapter processorAdapter;
    private final TransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.TRANSFER_QUEUE)
    public void consume(TransferMessage message) {
        log.info("Processing transfer: paymentReference={}", message.getPaymentReference());
        try {
            TransferRequest request = TransferRequest.builder()
                    .paymentReference(message.getPaymentReference())
                    .nameEnquiryRef(message.getNameEnquiryRef())
                    .accountNumber(message.getAccountNumber())
                    .bankCode(message.getBankCode())
                    .accountName(message.getAccountName())
                    .amount(message.getAmount())
                    .narration(message.getNarration())
                    .sourceAccount(message.getSourceAccount())
                    .build();

            TransferResult result = processorAdapter.performTransfer(request);

            ResponseMessage responseMessage = new ResponseMessage(
                    message.getTransactionId(),
                    message.getPaymentReference(),
                    message.getMerchantReference(),
                    message.getMerchantId(),
                    result.getProcessorReference(),
                    result.isSuccess(),
                    result.getResponseCode(),
                    result.getResponseMessage(),
                    result.getRemarks(),
                    result.getAmount()
            );
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYOUT_EXCHANGE,
                    "payout.response",
                    responseMessage
            );
        } catch (Exception e) {
            log.error("Transfer failed: paymentReference={}", message.getPaymentReference(), e);
            transactionRepository.updateStatus(
                    message.getTransactionId(),
                    "FAILED",
                    e.getMessage()
            );
            ResponseMessage failResponse = new ResponseMessage(
                    message.getTransactionId(),
                    message.getPaymentReference(),
                    message.getMerchantReference(),
                    message.getMerchantId(),
                    null,
                    false,
                    "99",
                    e.getMessage(),
                    "failed",
                    message.getAmount()
            );
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYOUT_EXCHANGE,
                    "payout.response",
                    failResponse
            );
            throw new RuntimeException(e);
        }
    }

    public record ResponseMessage(
            String transactionId,
            String paymentReference,
            String merchantReference,
            String merchantId,
            String processorReference,
            boolean success,
            String responseCode,
            String responseMessage,
            String remarks,
            BigDecimal amount
    ) {}
}
