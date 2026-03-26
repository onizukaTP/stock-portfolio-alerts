package com.bl.stockportfolioalerts.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePortfolioRequest {

    private List<HoldingRequest> holdings;
}