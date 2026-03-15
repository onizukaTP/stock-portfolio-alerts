package com.bl.stockportfolioalerts.messaging.rabbitmq;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertEvent implements Serializable {

    private Long alertId;
    private String ticker;
    private double thresholdPrice;
    private Long userId;

}