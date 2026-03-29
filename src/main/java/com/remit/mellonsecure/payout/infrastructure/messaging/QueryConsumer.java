package com.remit.mellonsecure.payout.infrastructure.messaging;

import com.remit.mellonsecure.payout.domain.model.TransactionQueryResult;
import com.remit.mellonsecure.payout.domain.port.ProcessorAdapter;
import com.remit.mellonsecure.payout.domain.port.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryConsumer {

    private final ProcessorAdapter processorAdapter;
    private final TransactionRepository transactionRepository;

    @RabbitListener(queues = RabbitMQConfig.QUERY_QUEUE)
    public void consume(ResponseHandler.QueryMessage message) {
        log.info("Querying transaction status: processorReference={}", message.processorReference());
        try {
            TransactionQueryResult result = processorAdapter.queryTransaction(message.processorReference());
            if (result != null && result.isSuccess()) {
                transactionRepository.updateStatus(
                        message.transactionId(),
                        result.getStatus().name(),
                        Optional.ofNullable(result.getResponseMessage()).orElse("")
                );
            }
        } catch (Exception e) {
            log.warn("Query failed for processorReference={}: {}", message.processorReference(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
