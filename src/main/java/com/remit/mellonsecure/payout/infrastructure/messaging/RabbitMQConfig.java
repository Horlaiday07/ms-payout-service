package com.remit.mellonsecure.payout.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TRANSFER_QUEUE = "payout.transfer.queue";
    public static final String RESPONSE_QUEUE = "payout.response.queue";
    public static final String QUERY_QUEUE = "payout.query.queue";
    public static final String WEBHOOK_QUEUE = "payout.webhook.queue";
    public static final String DEAD_LETTER_QUEUE = "payout.deadletter.queue";

    public static final String PAYOUT_EXCHANGE = "payout.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "payout.dlx";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        template.setMandatory(true);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setPrefetchCount(10);
        return factory;
    }

    @Bean
    public DirectExchange payoutExchange() {
        return new DirectExchange(PAYOUT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue transferQueue() {
        return QueueBuilder.durable(TRANSFER_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("payout.transfer.dlq")
                .build();
    }

    @Bean
    public Queue responseQueue() {
        return QueueBuilder.durable(RESPONSE_QUEUE).build();
    }

    @Bean
    public Queue queryQueue() {
        return QueueBuilder.durable(QUERY_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("payout.query.dlq")
                .build();
    }

    @Bean
    public Queue webhookQueue() {
        return QueueBuilder.durable(WEBHOOK_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey("payout.webhook.dlq")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding transferBinding() {
        return BindingBuilder.bind(transferQueue()).to(payoutExchange()).with("payout.transfer");
    }

    @Bean
    public Binding responseBinding() {
        return BindingBuilder.bind(responseQueue()).to(payoutExchange()).with("payout.response");
    }

    @Bean
    public Binding queryBinding() {
        return BindingBuilder.bind(queryQueue()).to(payoutExchange()).with("payout.query");
    }

    @Bean
    public Binding webhookBinding() {
        return BindingBuilder.bind(webhookQueue()).to(payoutExchange()).with("payout.webhook");
    }

    @Bean
    public Binding transferDlqBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("payout.transfer.dlq");
    }

    @Bean
    public Binding queryDlqBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("payout.query.dlq");
    }

    @Bean
    public Binding webhookDlqBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("payout.webhook.dlq");
    }
}
