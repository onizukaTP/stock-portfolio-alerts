package com.bl.stockportfolioalerts.portfolio.service;

import com.bl.stockportfolioalerts.auth.entity.User;
import com.bl.stockportfolioalerts.auth.repository.UserRepository;
import com.bl.stockportfolioalerts.portfolio.dto.HoldingRequest;
import com.bl.stockportfolioalerts.portfolio.entity.Holding;
import com.bl.stockportfolioalerts.portfolio.entity.Portfolio;
import com.bl.stockportfolioalerts.portfolio.repository.PortfolioRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public PortfolioService(PortfolioRepository portfolioRepository,
                            UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
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
}