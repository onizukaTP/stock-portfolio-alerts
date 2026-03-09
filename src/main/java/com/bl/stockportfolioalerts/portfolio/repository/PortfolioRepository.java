package com.bl.stockportfolioalerts.portfolio.repository;

import com.bl.stockportfolioalerts.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserId(Long userId);
}
