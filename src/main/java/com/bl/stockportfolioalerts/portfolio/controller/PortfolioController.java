package com.bl.stockportfolioalerts.portfolio.controller;

import com.bl.stockportfolioalerts.portfolio.dto.*;
import com.bl.stockportfolioalerts.portfolio.entity.Portfolio;
import com.bl.stockportfolioalerts.portfolio.service.PortfolioService;
import com.bl.stockportfolioalerts.portfolio.util.CsvParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping("/showall")
    public ResponseEntity<List<PortfolioResponse>> getAllPortfolios() {
        return ResponseEntity.ok(
                portfolioService.getAllPortfolios()
        );
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

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable Long id) {

        PortfolioResponse response = portfolioService.getPortfolio(id);

        response.add(linkTo(methodOn(PortfolioController.class)
                .getPortfolio(id)).withSelfRel());

        response.add(linkTo(methodOn(PortfolioController.class)
                .createPortfolio(null)).withRel("create"));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            @PathVariable Long id,
            @RequestBody UpdatePortfolioRequest request) {

        PortfolioResponse response = portfolioService.updatePortfolio(id, request);

        response.add(linkTo(methodOn(PortfolioController.class)
                .getPortfolio(id)).withSelfRel());

        return ResponseEntity.ok(response);
    }
    ;
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id) {

        log.info("API request received to delete portfolio id={}", id);

        portfolioService.deletePortfolio(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/stocks")
    public ResponseEntity<PortfolioResponse> addStock(
            @PathVariable Long id,
            @RequestBody AddStockRequest request) {

        log.info("API request received to add stock. portfolioId={}, ticker={}",
                id, request.getTicker());

        PortfolioResponse response = portfolioService.addStock(id, request);

        return ResponseEntity.ok(response);
    }
}