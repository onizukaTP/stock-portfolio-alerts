package com.bl.stockportfolioalerts.alert.service;

import com.bl.stockportfolioalerts.alert.dto.CreateAlertRequest;
import com.bl.stockportfolioalerts.alert.entity.Alert;
import com.bl.stockportfolioalerts.alert.repository.AlertRepository;
import com.bl.stockportfolioalerts.auth.entity.User;
import com.bl.stockportfolioalerts.auth.repository.UserRepository;
import com.bl.stockportfolioalerts.messaging.rabbitmq.AlertEvent;
import com.bl.stockportfolioalerts.messaging.rabbitmq.AlertEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final AlertEventPublisher alertEventPublisher;

    public Alert createAlert(CreateAlertRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Alert alert = Alert.builder()
                .ticker(request.getTicker())
                .thresholdPrice(request.getThresholdPrice())
                .user(user)
                .build();

        alertRepository.save(alert);

        log.info("Alert created for ticker={}, threshold={}",
                request.getTicker(), request.getThresholdPrice());

        AlertEvent event = AlertEvent.builder()
                .alertId(alert.getId())
                .ticker(alert.getTicker())
                .thresholdPrice(alert.getThresholdPrice())
                .userId(user.getId())
                .build();

        alertEventPublisher.publishAlertEvent(event);

        return alert;
    }
}