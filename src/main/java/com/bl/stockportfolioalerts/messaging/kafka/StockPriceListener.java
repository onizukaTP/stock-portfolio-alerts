package com.bl.stockportfolioalerts.messaging.kafka;

import com.bl.stockportfolioalerts.alert.entity.Alert;
import com.bl.stockportfolioalerts.alert.repository.AlertRepository;
import com.bl.stockportfolioalerts.messaging.rabbitmq.AlertEvent;
import com.bl.stockportfolioalerts.messaging.rabbitmq.AlertEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockPriceListener {

    private final AlertRepository alertRepository;
    private final AlertEventPublisher alertEventPublisher;

    @KafkaListener(topics = "stock-price", groupId = "stock-alert-group")
    public void consumeStockPrice(StockPriceEvent event) {

        log.info("Received stock price event ticker={} price={}",
                event.getTicker(), event.getPrice());

        List<Alert> alerts = alertRepository.findByTicker(event.getTicker());

        alerts.stream()
                .filter(alert -> event.getPrice() > alert.getThresholdPrice())
                .forEach(alert -> {

                    log.info("Alert triggered ticker={} threshold={}",
                            alert.getTicker(), alert.getThresholdPrice());

                    AlertEvent alertEvent = AlertEvent.builder()
                            .alertId(alert.getId())
                            .ticker(alert.getTicker())
                            .thresholdPrice(alert.getThresholdPrice())
                            .userId(alert.getUser().getId())
                            .build();

                    alertEventPublisher.publishAlertEvent(alertEvent);
                });
    }
}