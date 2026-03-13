package com.bl.stockportfolioalerts.stock.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockPriceResponse {

    private String ticker;
    private double price;

}