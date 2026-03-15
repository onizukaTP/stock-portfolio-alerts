package com.bl.stockportfolioalerts.alert.controller;

import com.bl.stockportfolioalerts.alert.dto.CreateAlertRequest;
import com.bl.stockportfolioalerts.alert.entity.Alert;
import com.bl.stockportfolioalerts.alert.service.AlertService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<Alert> createAlert(
            @RequestBody CreateAlertRequest request) {

        log.info("API request to create alert for ticker={}", request.getTicker());

        return ResponseEntity.ok(
                alertService.createAlert(request)
        );
    }
}