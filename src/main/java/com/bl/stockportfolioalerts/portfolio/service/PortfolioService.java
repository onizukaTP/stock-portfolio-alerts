package com.bl.stockportfolioalerts.portfolio.service;

import com.bl.stockportfolioalerts.auth.entity.User;
import com.bl.stockportfolioalerts.auth.repository.UserRepository;
import com.bl.stockportfolioalerts.portfolio.dto.HoldingRequest;
import com.bl.stockportfolioalerts.portfolio.dto.HoldingResponse;
import com.bl.stockportfolioalerts.portfolio.dto.PortfolioResponse;
import com.bl.stockportfolioalerts.portfolio.dto.UpdatePortfolioRequest;
import com.bl.stockportfolioalerts.portfolio.entity.Holding;
import com.bl.stockportfolioalerts.portfolio.entity.Portfolio;
import com.bl.stockportfolioalerts.portfolio.repository.HoldingRepository;
import com.bl.stockportfolioalerts.portfolio.repository.PortfolioRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            UserRepository userRepository,
                            HoldingRepository holdingRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.holdingRepository = holdingRepository;
    }

    public Portfolio createPortfolio(List<HoldingRequest> holdingRequests) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Portfolio portfolio = new Portfolio();
        portfolio.setUser(user);

        List<Holding> holdings = holdingRequests.stream()
                .map(req -> {
                    Holding h = new Holding();
                    h.setTicker(req.getTicker());
                    h.setQuantity(req.getQuantity());
                    h.setBuyingPrice(req.getBuyingPrice());
                    h.setPortfolio(portfolio);
                    return h;
                })
                .collect(Collectors.toList());

        double total = holdings.stream()
                .mapToDouble(Holding::getValue)
                .sum();

        portfolio.setHoldings(holdings);
        portfolio.setTotalValue(total);

        return portfolioRepository.save(portfolio);
    }

    public PortfolioResponse getPortfolio(Long portfolioId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        if (!portfolio.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access");
        }

        List<HoldingResponse> holdings = portfolio.getHoldings()
                .stream()
                .filter(h -> h.getQuantity() > 0)
                .map(h -> new HoldingResponse(
                        h.getTicker(),
                        h.getQuantity(),
                        h.getBuyingPrice(),
                        h.getValue()
                ))
                .toList();

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getTotalValue(),
                holdings
        );
    }

    @Transactional
    public PortfolioResponse updatePortfolio(Long portfolioId, UpdatePortfolioRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        if (!portfolio.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Unauthorized access");
        }

        holdingRepository.deleteByPortfolio_Id(portfolioId);

        List<Holding> holdings = request.getHoldings()
                .stream()
                .map(req -> {
                    Holding h = new Holding();
                    h.setTicker(req.getTicker());
                    h.setQuantity(req.getQuantity());
                    h.setBuyingPrice(req.getBuyingPrice());
                    h.setPortfolio(portfolio);
                    return h;
                })
                .collect(Collectors.toList());

        portfolio.setHoldings(holdings);

        double total = holdings.stream()
                .mapToDouble(Holding::getValue)
                .sum();

        portfolio.setTotalValue(total);

        portfolioRepository.save(portfolio);

        List<HoldingResponse> holdingResponses = holdings.stream()
                .map(h -> new HoldingResponse(
                        h.getTicker(),
                        h.getQuantity(),
                        h.getBuyingPrice(),
                        h.getValue()
                ))
                .collect(Collectors.toList());

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getTotalValue(),
                holdingResponses
        );
    }

    @Transactional
    public void deletePortfolio(Long portfolioId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        log.info("Delete portfolio request received. portfolioId={}, user={}", portfolioId, email);

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> {
                    log.error("Portfolio not found. portfolioId={}", portfolioId);
                    return new RuntimeException("Portfolio not found");
                });

        if (!portfolio.getUser().getEmail().equals(email)) {
            log.warn("Unauthorized delete attempt. portfolioId={}, user={}", portfolioId, email);
            throw new RuntimeException("Unauthorized access");
        }

        portfolioRepository.delete(portfolio);

        log.info("Portfolio deleted successfully. portfolioId={}, user={}", portfolioId, email);
    }
}