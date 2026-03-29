package com.remit.mellonsecure.payout.infrastructure.messaging;

import com.remit.mellonsecure.payout.domain.model.TransferMessage;
import com.remit.mellonsecure.payout.domain.port.TransferPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransferPublisherAdapter implements TransferPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(TransferMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYOUT_EXCHANGE,
                "payout.transfer",
                message
        );
        log.info("Published transfer message: paymentReference={}", message.getPaymentReference());
    }
}
