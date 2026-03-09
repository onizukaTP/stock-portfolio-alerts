package com.bl.stockportfolioalerts.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PortfolioResponse extends RepresentationModel<PortfolioResponse> {

    private Long portfolioId;
    private double totalValue;
    private List<HoldingResponse> holdings;
}