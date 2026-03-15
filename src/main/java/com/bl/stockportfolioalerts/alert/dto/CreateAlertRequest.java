package com.bl.stockportfolioalerts.alert.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAlertRequest {

    private String ticker;
    private double thresholdPrice;

}