package com.bl.stockportfolioalerts.messaging.rabbitmq;

import org.springframework.context.annotation.*;
import org.springframework.amqp.core.Queue;

@Configuration
public class RabbitMQConfig {

    public static final String ALERT_QUEUE = "stock-alert-queue";

    @Bean
    public Queue alertQueue() {
        return new Queue(ALERT_QUEUE, true);
    }
}