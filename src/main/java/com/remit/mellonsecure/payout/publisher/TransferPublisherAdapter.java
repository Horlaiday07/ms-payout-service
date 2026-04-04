package com.remit.mellonsecure.payout.publisher;

import com.remit.mellonsecure.payout.config.RabbitMQConfig;
import com.remit.mellonsecure.payout.entity.TransferMessage;
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
