package com.bl.stockportfolioalerts.messaging.rabbitmq;

import com.bl.stockportfolioalerts.notification.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.ALERT_QUEUE)
    public void consumeAlertEvent(AlertEvent event) {

        log.info("Received alert event from RabbitMQ: ticker={}, threshold={}",
                event.getTicker(), event.getThresholdPrice());

        String message = String.format(
                "Stock %s crossed threshold %.2f",
                event.getTicker(),
                event.getThresholdPrice()
        );

        notificationService.sendNotification(message);
    }
}