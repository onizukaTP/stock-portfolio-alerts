package com.bl.stockportfolioalerts.messaging.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishAlertEvent(AlertEvent event) {

        log.info("Publishing alert event to RabbitMQ: ticker={}", event.getTicker());

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ALERT_QUEUE,
                event
        );
    }
}