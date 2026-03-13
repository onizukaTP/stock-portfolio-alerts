package com.bl.stockportfolioalerts.portfolio.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddStockRequest {

    private String ticker;
    private int quantity;

}