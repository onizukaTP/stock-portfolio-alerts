package com.bl.stockportfolioalerts.portfolio.dto;

import lombok.Data;

@Data
public class HoldingRequest {

    private String ticker;
    private int quantity;
    private double buyingPrice;
}