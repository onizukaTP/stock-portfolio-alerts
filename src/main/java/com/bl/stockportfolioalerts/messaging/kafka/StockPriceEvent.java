package com.bl.stockportfolioalerts.messaging.kafka;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockPriceEvent {

    private String ticker;
    private double price;

}