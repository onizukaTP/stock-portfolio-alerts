package com.bl.stockportfolioalerts.portfolio.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePortfolioRequest {

    private List<HoldingRequest> holdings;

}