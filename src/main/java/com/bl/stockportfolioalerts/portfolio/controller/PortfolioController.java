package com.bl.stockportfolioalerts.portfolio.controller;

import com.bl.stockportfolioalerts.portfolio.dto.*;
import com.bl.stockportfolioalerts.portfolio.service.PortfolioService;
import com.bl.stockportfolioalerts.portfolio.util.CsvParser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    public ResponseEntity<?> createPortfolio(@RequestBody CreatePortfolioRequest request) {

        return ResponseEntity.ok(
                portfolioService.createPortfolio(request.getHoldings())
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPortfolio(@RequestParam MultipartFile file) {

        return ResponseEntity.ok(
                portfolioService.createPortfolio(
                        CsvParser.parse(file)
                )
        );
    }
}