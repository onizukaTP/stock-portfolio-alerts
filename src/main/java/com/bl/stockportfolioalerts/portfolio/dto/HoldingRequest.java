package com.bl.stockportfolioalerts.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldingRequest {

    private String ticker;
    private int quantity;
    private double buyingPrice;
}