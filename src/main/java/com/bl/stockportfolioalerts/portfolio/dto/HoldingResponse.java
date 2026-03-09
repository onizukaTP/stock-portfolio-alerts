package com.bl.stockportfolioalerts.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HoldingResponse {

    private String ticker;
    private int quantity;
    private double buyingPrice;
    private double value;
}