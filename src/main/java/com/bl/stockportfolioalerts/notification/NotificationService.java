package com.bl.stockportfolioalerts.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    public void sendNotification(String message) {

        // Simulated notification (UC11 requirement)
        log.info("NOTIFICATION SENT: {}", message);
    }
}