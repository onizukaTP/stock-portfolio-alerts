package com.bl.stockportfolioalerts.portfolio.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreatePortfolioRequest {

    private List<HoldingRequest> holdings;
}